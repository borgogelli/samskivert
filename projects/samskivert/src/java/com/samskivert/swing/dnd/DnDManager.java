//
// $Id: DnDManager.java,v 1.7 2002/09/06 00:26:10 ray Exp $

package com.samskivert.swing.dnd;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;

import java.awt.event.AWTEventListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;

import javax.swing.JComponent;

import javax.swing.event.AncestorEvent;

import com.samskivert.swing.event.AncestorAdapter;
import com.samskivert.Log;

/**
 * A custom Drag and Drop manager for use within a single JVM. Does what we
 * need it to do and no more.
 */
public class DnDManager
    implements MouseMotionListener, AWTEventListener
{
    /**
     * Add the specified component as a source of drags, with the DragSource
     * controller.
     */
    public static void addDragSource (DragSource source, JComponent comp)
    {
        singleton.addSource(source, comp);
    }

    /**
     * Add the specified component as a drop target.
     */
    public static void addDropTarget (DropTarget target, JComponent comp)
    {
        singleton.addTarget(target, comp);
    }

    /**
     * Create a custom cursor out of the specified image.
     */
    public static Cursor createImageCursor (Image img)
    {
        // TODO: check colors/size
        Toolkit tk = Toolkit.getDefaultToolkit();
        return tk.createCustomCursor(img,
            new Point(img.getWidth(null) / 2, img.getHeight(null) / 2),
            "samskivertDnDCursor");
    }

    /**
     * Restrict construction.
     */
    private DnDManager ()
    {
    }

    /**
     * Add a dragsource.
     */
    protected void addSource (DragSource source, JComponent comp)
    {
        _draggers.put(comp, source);
        comp.addAncestorListener(_remover);
        comp.addMouseMotionListener(this);
    }

    /**
     * Add a droptarget.
     */
    protected void addTarget (DropTarget target, JComponent comp)
    {
        _droppers.put(comp, target);
        comp.addAncestorListener(_remover);
    }

    // documentation inherited from interface MouseMotionListener
    public void mouseMoved (MouseEvent me)
    {
        // who cares.
    }

    // documentation inherited from interface MouseMotionListener
    public void mouseDragged (MouseEvent me)
    {
        // make sure a drag hasn't already started.
        if (_sourceComp != null) {
            return;
        }

        _sourceComp = me.getComponent();
        _source = (DragSource) _draggers.get(_sourceComp);

        // make sure the source wants to start a drag.
        if ((_source == null) || (!_source.startDrag(_cursors, _data))) {
            // if not, reset our start conditions and bail
            reset();
            return;
        }

        // use standard cursors if custom ones not specified
        if (_cursors[0] == null) {
            _cursors[0] = java.awt.dnd.DragSource.DefaultMoveDrop;
        }
        if (_cursors[1] == null) {
            _cursors[1] = java.awt.dnd.DragSource.DefaultMoveNoDrop;
        }

        // start out with the no-drop cursor.
        _curCursor = _cursors[1];

        // install a listener so we know everywhere that the mouse enters
        Toolkit.getDefaultToolkit().addAWTEventListener(this, 
                AWTEvent.MOUSE_EVENT_MASK);

        // find the top-level window and set the cursor there.
        for (_topComp = _sourceComp; true; ) {
            Component c = _topComp.getParent();
            if (c == null) {
                break;
            }
            _topComp = c;
        }
        _topCursor = _topComp.getCursor();
        _topComp.setCursor(_curCursor);

        setComponentCursor(_sourceComp);
    }

    /**
     * Check to see if we need to do component-level cursor setting and take
     * care of it if needed.
     */
    protected void setComponentCursor (Component comp)
    {
        Cursor c = comp.getCursor();
        if (c != _curCursor) {
            _lastComp = comp;
            _oldCursor = comp.isCursorSet() ? c : null;
            comp.setCursor(_curCursor);
        }
    }

    /**
     * Clear out the component-level cursor.
     */
    protected void clearComponentCursor ()
    {
        if (_lastComp != null) {
            _lastComp.setCursor(_oldCursor);
            _lastComp = null;
        }
    }

    // documentation inherited from interface AWTEventListener
    public void eventDispatched (AWTEvent event)
    {
        switch (event.getID()) {
        case MouseEvent.MOUSE_ENTERED:
            mouseEntered((MouseEvent) event);
            break;

        case MouseEvent.MOUSE_EXITED:
            mouseExited((MouseEvent) event);
            break;

        case MouseEvent.MOUSE_RELEASED:
            mouseReleased((MouseEvent) event);
            break;
        }
    }

    /**
     * Handle the mouse entering a new component.
     */
    protected void mouseEntered (MouseEvent event)
    {
        Component newcomp = ((MouseEvent) event).getComponent();
        _lastTarget = findAppropriateTarget(newcomp);
        Cursor newcursor = _cursors[(_lastTarget == null) ? 1 : 0];

        // see if the current cursor changed.
        if (newcursor != _curCursor) {
            _topComp.setCursor(_curCursor = newcursor);
        }

        // and check the cursor at the component level
        setComponentCursor(newcomp);
    }

    /**
     * Handle the mouse leaving a component.
     */
    protected void mouseExited (MouseEvent event)
    {
        clearComponentCursor();

        // and if we were over a target, let the target know that we left
        if (_lastTarget != null) {
            _lastTarget.noDrop();
            _lastTarget = null;
        }
    }

    /**
     * Handle the mouse button being released.
     */
    protected void mouseReleased (MouseEvent event)
    {
        // stop listening to every little event
        Toolkit.getDefaultToolkit().removeAWTEventListener(this);

        // reset cursors
        clearComponentCursor();
        _topComp.setCursor(_topCursor);

        // the event.getComponent() will be the source component here (huh..)
        // so we instead use the last component seen in mouseEnter
        if (_lastTarget != null) {
            _lastTarget.dropCompleted(_source, _data[0]);
            _source.dragCompleted(_lastTarget);
        }
        reset();
    }

    /**
     * Find the lowest accepting parental target to this component.
     */
    protected DropTarget findAppropriateTarget (Component comp)
    {
        Component parent;
        DropTarget target;
        while (true) {
            // here we sneakily prevent dropping on the source
            target = (comp == _sourceComp) ? null
                                           : (DropTarget) _droppers.get(comp);
            if ((target != null) && target.checkDrop(_source, _data[0])) {
                return target;
            }
            parent = comp.getParent();
            if (parent == null) {
                return null;
            }
            comp = parent;
        }
    }

    /**
     * Reset dnd to a starting state.
     */
    protected void reset ()
    {
        _source = null;
        _sourceComp = null;
        _lastComp = null;
        _lastTarget = null;
        _data[0] = null;
        _cursors[0] = null;
        _cursors[1] = null;
        _topComp = null;
        _topCursor = null;
        _curCursor = null;
    }

    /** A handy helper that removes components when they're no longer in
     * the hierarchy. */
    protected AncestorAdapter _remover = new AncestorAdapter() {
        public void ancestorRemoved (AncestorEvent ae)
        {
            JComponent comp = ae.getComponent();
            _draggers.remove(comp);
            _droppers.remove(comp);
        }
    };

    /** Our DropTargets, indexed by associated Component. */
    protected HashMap _droppers = new HashMap();

    /** Our DragSources, indexed by associated component. */
    protected HashMap _draggers = new HashMap();

    /** The original, last, and top-level components during a drag. */
    protected Component _sourceComp, _lastComp, _topComp;

    /** The source of a drag. */
    protected DragSource _source;

    /** The last target, or null if no last target. */
    protected DropTarget _lastTarget;

    /** The current cursor we're showing the user. */
    protected Cursor _curCursor;

    /** The cursor that used to be set for _lastComp. */
    protected Cursor _oldCursor;

    /** The original top-level cursor. */
    protected Cursor _topCursor;

    /** The accept/reject cursors. */
    protected Cursor[] _cursors = new Cursor[2];

    /** The data to be passed in the drop. */
    protected Object[] _data = new Object[1];

    /** A single manager for the entire JVM. */
    protected static final DnDManager singleton = new DnDManager();
}