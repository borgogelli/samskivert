//
// $Id

package com.threerings.venison;

import java.awt.event.ActionEvent;

import com.threerings.presents.dobj.AttributeChangedEvent;
import com.threerings.presents.dobj.DSet;
import com.threerings.presents.dobj.ElementAddedEvent;
import com.threerings.presents.dobj.ElementRemovedEvent;
import com.threerings.presents.dobj.ElementUpdatedEvent;
import com.threerings.presents.dobj.MessageEvent;
import com.threerings.presents.dobj.SetListener;

import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.client.PlaceView;

import com.threerings.parlor.turn.TurnGameController;
import com.threerings.parlor.util.ParlorContext;

import com.threerings.venison.Log;

/**
 * The main coordinator of user interface activities on the client-side of
 * the Venison game.
 */
public class VenisonController
    extends TurnGameController implements VenisonCodes, SetListener
{
    // documentation inherited
    protected void didInit ()
    {
        super.didInit();

        // get a handle on our body object
        _self = (BodyObject)_ctx.getClient().getClientObject();
    }

    // documentation inherited
    protected PlaceView createPlaceView ()
    {
        _panel = new VenisonPanel(_ctx, this);
        return _panel;
    }

    // documentation inherited
    public void willEnterPlace (PlaceObject plobj)
    {
        super.willEnterPlace(plobj);

        // get a casted reference to our game object
        _venobj = (VenisonObject)plobj;

        // grab the tiles and piecens from the game object and configure
        // the board with them
        _panel.board.setTiles(_venobj.tiles);
        _panel.board.setPiecens(_venobj.piecens);

        // TBD: check to see if it's our turn and set things up
        // accordingly
    }

    // documentation inherited
    protected void turnDidChange (String turnHolder)
    {
        super.turnDidChange(turnHolder);

        // if it's our turn, set the tile to be placed. otherwise clear it
        // out
        if (turnHolder.equals(_self.username)) {
            Log.info("Setting tile to be placed: " + _venobj.currentTile);
            _panel.board.setTileToBePlaced(_venobj.currentTile);
        }
    }

    // documentation inherited
    public void attributeChanged (AttributeChangedEvent event)
    {
        super.attributeChanged(event);

        // handle the setting of the board state
        if (event.getName().equals(VenisonObject.TILES)) {
            _panel.board.setTiles(_venobj.tiles);

        } else if (event.getName().equals(VenisonObject.PIECENS)) {
            _panel.board.setPiecens(_venobj.piecens);
        }
    }

    // documentation inherited
    public void elementAdded (ElementAddedEvent event)
    {
        // we care about additions to TILES and PIECENS
        if (event.getName().equals(VenisonObject.TILES)) {
            // a tile was added, add it to the board
            VenisonTile tile = (VenisonTile)event.getElement();
            _panel.board.addTile(tile);

        } else if (event.getName().equals(VenisonObject.PIECENS)) {
            // a piecen was added, place it on the board
            Piecen piecen = (Piecen)event.getElement();
            _panel.board.placePiecen(piecen);
        }
    }

    // documentation inherited
    public void elementUpdated (ElementUpdatedEvent event)
    {
    }

    // documentation inherited
    public void elementRemoved (ElementRemovedEvent event)
    {
    }

    // documentation inherited
    public boolean handleAction (ActionEvent action)
    {
        if (action.getActionCommand().equals(TILE_PLACED)) {
            // the user placed the tile into a valid location. grab the
            // placed tile from the board and submit it to the server
            Object[] args = new Object[] { _panel.board.getPlacedTile() };
            MessageEvent mevt = new MessageEvent(
                _venobj.getOid(), PLACE_TILE_REQUEST, args);
            _ctx.getDObjectManager().postEvent(mevt);

            // enable the noplace button
            _panel.noplace.setEnabled(true);

        } else if (action.getActionCommand().equals(PIECEN_PLACED)) {
            // the user placed a piecen on the tile. grab the piecen from
            // the placed tile and submit it to the server
            Object[] args = new Object[] {
                _panel.board.getPlacedTile().piecen };
            MessageEvent mevt = new MessageEvent(
                _venobj.getOid(), PLACE_PIECEN_REQUEST, args);
            _ctx.getDObjectManager().postEvent(mevt);

            // disable the noplace button
            _panel.noplace.setEnabled(false);

        } else if (action.getActionCommand().equals(PLACE_NOTHING)) {
            // the user doesn't want to place anything this turn. send a
            // place nothing request to the server
            MessageEvent mevt = new MessageEvent(
                _venobj.getOid(), PLACE_NOTHING_REQUEST, null);
            _ctx.getDObjectManager().postEvent(mevt);

            // disable the noplace button
            _panel.noplace.setEnabled(false);

        } else {
            return super.handleAction(action);
        }

        return true;
    }

    /** A reference to our game panel. */
    protected VenisonPanel _panel;

    /** A reference to our game panel. */
    protected VenisonObject _venobj;

    /** A reference to our body object. */
    protected BodyObject _self;
}
