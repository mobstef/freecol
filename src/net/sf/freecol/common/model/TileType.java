/**
 *  Copyright (C) 2002-2013   The FreeCol Team
 *
 *  This file is part of FreeCol.
 *
 *  FreeCol is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  FreeCol is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with FreeCol.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.sf.freecol.common.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import net.sf.freecol.common.io.FreeColXMLReader;
import net.sf.freecol.common.io.FreeColXMLWriter;
import net.sf.freecol.common.option.OptionGroup;
import net.sf.freecol.common.option.StringOption;
import net.sf.freecol.common.util.RandomChoice;


/**
 * The types of tiles.
 */
public final class TileType extends FreeColGameObjectType {

    public static enum RangeType { HUMIDITY, TEMPERATURE, ALTITUDE };

    /**
     * Use these tile types only for "land maps", i.e. maps that only
     * distinguish water and land.
     */
    public static final TileType WATER = new TileType("WATER", true);
    public static final TileType LAND  = new TileType("LAND", false);

    /** Is this a forested tile? */
    private boolean forest;

    /** Is this a water tile? */
    private boolean water;

    /** Can this tile be settled? */
    private boolean canSettle;

    /** Whether this TileType is connected to Europe. */
    private boolean connected;

    /** Is this elevated terrain? */
    private boolean elevation;

    /** The base movement cost for this tile type. */
    private int basicMoveCost;

    /** The base work turns for this tile type. */
    private int basicWorkTurns;

    /** The humidity range for this tile type. */
    private int[] humidity = new int[2];
    /** The temperature range for this tile type. */
    private int[] temperature = new int[2];
    /** The altitude range for this tile type. */
    private int[] altitude = new int[2];

    /** The resource types that are valid for this tile type. */
    private List<RandomChoice<ResourceType>> resourceTypes = null;

    /** The disasters that may strike this type of tile. */
    private List<RandomChoice<Disaster>> disasters = null;

    /**
     * The possible production types of this tile type.  This includes
     * the production types available if a tile of this type is a
     * colony center tile.
     */
    private List<ProductionType> productionTypes = null;


    /**
     * Create a new tile type.
     *
     * @param id The object identifier.
     * @param specification The <code>Specification</code> to refer to.
     */
    public TileType(String id, Specification specification) {
        super(id, specification);
    }

    /**
     * Creates a new <code>TileType</code> instance. This constructor
     * is used to create the "virtual" tile types <code>LAND</code>
     * and <code>WATER</code>, which are intended to simplify map
     * loading.
     *
     * @param id The object identifier.
     * @param water True if this is a water tile.
     */
    private TileType(String id, boolean water) {
        super(id, null);
        this.water = water;
    }


    /**
     * Is this tile type forested?
     *
     * @return True if this is a forested tile type.
     */
    public boolean isForested() {
        return forest;
    }

    /**
     * Is this a water tile type?
     *
     * @return True if this is a water tile type.
     */
    public boolean isWater() {
        return water;
    }

    /**
     * Can this tile type be settled?
     *
     * @return True if this is a settleable tile type.
     */
    public boolean canSettle() {
        return canSettle;
    }

    /**
     * Is this tile type connected to the high seas, by definition.
     *
     * @return True if the tile type is inherently connected to the high seas.
     */
    public boolean isHighSeasConnected() {
        return connected;
    }

    /**
     * Is this tile type directly connected to the high seas, that is, a
     * unit on a tile of this type can move immediately to the high seas.
     *
     * @return True if the tile type is directly connected.
     */
    public boolean isDirectlyHighSeasConnected() {
        return hasAbility(Ability.MOVE_TO_EUROPE);
    }

    /**
     * Is this an elevated tile type?
     *
     * @return True if this is an elevated tile type.
     */
    public boolean isElevation() {
        return elevation;
    }

    /**
     * Gets the basic movement cost through this tile type.
     *
     * @return The basic movement cost.
     */
    public int getBasicMoveCost() {
        return basicMoveCost;
    }

    /**
     * Gets the basic work turns to build an improvement on this tile type.
     *
     * @return The basic work turns.
     */
    public int getBasicWorkTurns() {
        return basicWorkTurns;
    }

    /**
     * Is this tile type suitable for a given range type value.
     *
     * @param rangeType The <code>RangeType</code> to test.
     * @param value The value to check.
     * @return True if the tile type meets the range limits.
     */
    public boolean withinRange(RangeType rangeType, int value) {
        switch (rangeType) {
        case HUMIDITY:
            return humidity[0] <= value && value <= humidity[1];
        case TEMPERATURE:
            return temperature[0] <= value && value <= temperature[1];
        case ALTITUDE:
            return altitude[0] <= value && value <= altitude[1];
        default:
            break;
        }
        return false;
    }

    /**
     * Gets the resources that can be placed on this tile type.
     *
     * @return A weighted list of resource types.
     */
    public List<RandomChoice<ResourceType>> getWeightedResources() {
        if (resourceTypes == null) return Collections.emptyList();
        return resourceTypes;
    }

    /**
     * Gets the resource types that can be found on this tile type.
     *
     * @return A list of <code>ResourceType</code>s.
     */
    public List<ResourceType> getResourceTypes() {
        List<ResourceType> result = new ArrayList<ResourceType>();
        if (resourceTypes != null) {
            for (RandomChoice<ResourceType> resource : resourceTypes) {
                result.add(resource.getObject());
            }
        }
        return result;
    }

    /**
     * Add a resource type.
     *
     * @param type The <code>ResourceType</code> to add.
     * @param probability The percentage probability of the resource
     *     being present.
     */
    private void addResourceType(ResourceType type, int probability) {
        if (resourceTypes == null) {
            resourceTypes = new ArrayList<RandomChoice<ResourceType>>();
        }
        resourceTypes.add(new RandomChoice<ResourceType>(type, probability));
    }

    /**
     * Can this tile type contain a specified resource type?
     *
     * @param resourceType The <code>ResourceType</code> to test.
     * @return True if the <code>ResourceType</code> is compatible.
     */
    public boolean canHaveResourceType(ResourceType resourceType) {
        return getResourceTypes().contains(resourceType);
    }

    /**
     * Gets the natural disasters than can strike this tile type.
     *
     * @return a <code>List<RandomChoice<Disaster>></code> value
     */
    public List<RandomChoice<Disaster>> getDisasters() {
        if (disasters == null) return Collections.emptyList();
        return disasters;
    }

    /**
     * Add a disaster.
     *
     * @param disaster The <code>Disaster</code> to add.
     * @param probability The probability of the disaster.
     */
    private void addDisaster(Disaster disaster, int probability) {
        if (disasters == null) {
            disasters = new ArrayList<RandomChoice<Disaster>>();
        }
        disasters.add(new RandomChoice<Disaster>(disaster, probability));
    }

    /**
     * Add a production type.
     *
     * @param productionType The <code>ProductionType</code> to add.
     */
    private void addProductionType(ProductionType productionType) {
        if (productionTypes == null) {
            productionTypes = new ArrayList<ProductionType>();
        }
        productionTypes.add(productionType);
    }

    /**
     * Gets the production types applicable to this tile type.
     *
     * @return A list of <code>ProductionType</code>s.
     */
    public List<ProductionType> getProductionTypes() {
        if (productionTypes == null) return Collections.emptyList();
        return productionTypes;
    }

    /**
     * Gets the production types available at the current difficulty
     * level.
     *
     * @param center Whether the tile is a colony center tile.
     * @return A list of <code>ProductionType</code>s.
     */
    public List<ProductionType> getProductionTypes(boolean center) {
        return getProductionTypes(center, 
            getSpecification().getString(GameOptions.TILE_PRODUCTION));
    }

    /**
     * Gets the production types available for the given combination
     * of colony center tile and production level.  If the production
     * level is null, all production levels will be returned.
     *
     * @param unattended Whether the production is unattended.
     * @param level The production level.
     * @return A list of <code>ProductionType</code>s.
     */
    public List<ProductionType> getProductionTypes(boolean unattended,
                                                   String level) {
        List<ProductionType> result = new ArrayList<ProductionType>();
        if (productionTypes != null) {
            for (ProductionType productionType : productionTypes) {
                if (productionType.isUnattended() == unattended
                    && productionType.appliesTo(level)) {
                    result.add(productionType);
                }
            }
        }
        return result;
    }


    // Utilities

    /**
     * Get the defence modifiers applicable to this tile type.
     *
     * @return A set of defense <code>Modifier</code>s.
     */
    public Set<Modifier> getDefenceModifiers() {
        return getModifierSet(Modifier.DEFENCE);
    }

    /**
     * Returns the amount of goods of given goods type the given unit
     * type could produce on a tile of this tile type.
     *
     * @param goodsType The <code>GoodsType</code> to produce.
     * @param unitType A <code>UnitType</code> that is to do the work.
     * @return The amount of goods production.
     */
    public int getProductionOf(GoodsType goodsType, UnitType unitType) {
        if (goodsType == null) return 0;
        int production = getProductionOf(goodsType);
        return (int)applyModifier(production, goodsType.getId(), unitType);
    }

    /**
     * Returns the amount of goods of given goods type this tile type can
     * produce.
     *
     * @param goodsType The <code>GoodsType</code> to produce.
     * @return The amount of goods production.
     */
    public int getProductionOf(GoodsType goodsType) {
        int amount = 0;
        for (ProductionType productionType : getProductionTypes(false)) {
            for (AbstractGoods output : productionType.getOutputs()) {
                if (output.getType() == goodsType) {
                    int newAmount = output.getAmount();
                    if (newAmount > amount) {
                        amount = newAmount;
                    }
                }
            }
        }
        return amount;
    }

    /**
     * Get all possible goods produced at a tile of this type.
     *
     * Used by static tile type displays that just list unattended
     * production values.  Planning and production routines should use
     * {@link getPotentialProduction(GoodsType, UnitType)}
     *
     * @return A list of produced <code>AbstractGoods</code>.
     */
    public List<AbstractGoods> getPossibleProduction() {
        List<AbstractGoods> production = new ArrayList<AbstractGoods>();
        for (ProductionType productionType : getProductionTypes(true)) {
            List<AbstractGoods> outputs = productionType.getOutputs();
            if (!outputs.isEmpty()) production.addAll(outputs);
        }
        return production;
    }

    /**
     * {@inheritDoc}
     *
     * Kludge to make this public so that MapViewer can see it.
     */
    @Override
    public int getIndex() {
        return super.getIndex();
    }


    // Serialization

    private static final String ALTITUDE_MIN_TAG = "altitudeMin";
    private static final String ALTITUDE_MAX_TAG = "altitudeMax";
    private static final String BASIC_MOVE_COST_TAG = "basic-move-cost";
    private static final String BASIC_WORK_TURNS_TAG = "basic-work-turns";
    private static final String CAN_SETTLE_TAG = "can-settle";
    private static final String DISASTER_TAG = "disaster";
    private static final String GEN_TAG = "gen";
    private static final String GOODS_TYPE_TAG = "goods-type";
    private static final String HUMIDITY_MIN_TAG = "humidityMin";
    private static final String HUMIDITY_MAX_TAG = "humidityMax";
    private static final String IS_CONNECTED_TAG = "is-connected";
    private static final String IS_ELEVATION_TAG = "is-elevation";
    private static final String IS_FOREST_TAG = "is-forest";
    private static final String IS_WATER_TAG = "is-water";
    private static final String PROBABILITY_TAG = "probability";
    private static final String PRODUCTION_TAG = "production";
    private static final String RESOURCE_TAG = "resource";
    private static final String TEMPERATURE_MIN_TAG = "temperatureMin";
    private static final String TEMPERATURE_MAX_TAG = "temperatureMax";
    private static final String TILE_PRODUCTION_TAG = "tile-production";
    private static final String TYPE_TAG = "type";
    // @compat 0.10.x
    private static final String PRIMARY_PRODUCTION_TAG = "primary-production";
    private static final String SECONDARY_PRODUCTION_TAG = "secondary-production";
    // end @compat 0.10.x


    /**
     * {@inheritDoc}
     */
    @Override
    protected void writeAttributes(FreeColXMLWriter xw) throws XMLStreamException {
        super.writeAttributes(xw);

        xw.writeAttribute(BASIC_MOVE_COST_TAG, basicMoveCost);

        xw.writeAttribute(BASIC_WORK_TURNS_TAG, basicWorkTurns);

        xw.writeAttribute(IS_FOREST_TAG, forest);

        xw.writeAttribute(IS_WATER_TAG, water);

        xw.writeAttribute(IS_ELEVATION_TAG, elevation);

        xw.writeAttribute(IS_CONNECTED_TAG, connected);

        xw.writeAttribute(CAN_SETTLE_TAG, canSettle);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void writeChildren(FreeColXMLWriter xw) throws XMLStreamException {
        super.writeChildren(xw);

        xw.writeStartElement(GEN_TAG);

        xw.writeAttribute(HUMIDITY_MIN_TAG, humidity[0]);

        xw.writeAttribute(HUMIDITY_MAX_TAG, humidity[1]);

        xw.writeAttribute(TEMPERATURE_MIN_TAG, temperature[0]);

        xw.writeAttribute(TEMPERATURE_MAX_TAG, temperature[1]);

        xw.writeAttribute(ALTITUDE_MIN_TAG, altitude[0]);

        xw.writeAttribute(ALTITUDE_MAX_TAG, altitude[1]);

        xw.writeEndElement();

        for (ProductionType productionType : getProductionTypes()) {
            productionType.toXML(xw);
        }

        for (RandomChoice<ResourceType> choice : getWeightedResources()) {
            xw.writeStartElement(RESOURCE_TAG);

            xw.writeAttribute(TYPE_TAG, choice.getObject());

            xw.writeAttribute(PROBABILITY_TAG, choice.getProbability());

            xw.writeEndElement();
        }

        for (RandomChoice<Disaster> choice : getDisasters()) {
            xw.writeStartElement(DISASTER_TAG);

            xw.writeAttribute(ID_ATTRIBUTE_TAG, choice.getObject());

            xw.writeAttribute(PROBABILITY_TAG, choice.getProbability());

            xw.writeEndElement();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void readAttributes(FreeColXMLReader xr) throws XMLStreamException {
        super.readAttributes(xr);

        basicMoveCost = xr.getAttribute(BASIC_MOVE_COST_TAG, 1);

        basicWorkTurns = xr.getAttribute(BASIC_WORK_TURNS_TAG, 1);

        forest = xr.getAttribute(IS_FOREST_TAG, false);

        water = xr.getAttribute(IS_WATER_TAG, false);

        elevation = xr.getAttribute(IS_ELEVATION_TAG, false);

        canSettle = xr.getAttribute(CAN_SETTLE_TAG, !water);

        connected = xr.getAttribute(IS_CONNECTED_TAG, false);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void readChildren(FreeColXMLReader xr) throws XMLStreamException {
        // Clear containers.
        if (xr.shouldClearContainers()) {
            disasters = null;
            resourceTypes = null;
            productionTypes = null;
        }

        super.readChildren(xr);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void readChild(FreeColXMLReader xr) throws XMLStreamException {
        final Specification spec = getSpecification();
        final String tag = xr.getLocalName();

        if (DISASTER_TAG.equals(tag)) {
            Disaster d = xr.getType(spec, ID_ATTRIBUTE_TAG,
                                    Disaster.class, (Disaster)null);
            if (d != null) {
                addDisaster(d, xr.getAttribute(PROBABILITY_TAG, 100));
            }
            xr.closeTag(DISASTER_TAG);

        } else if (GEN_TAG.equals(tag)) {
            humidity[0] = xr.getAttribute(HUMIDITY_MIN_TAG, 0);
            humidity[1] = xr.getAttribute(HUMIDITY_MAX_TAG, 100);
            temperature[0] = xr.getAttribute(TEMPERATURE_MIN_TAG, -20);
            temperature[1] = xr.getAttribute(TEMPERATURE_MAX_TAG, 40);
            altitude[0] = xr.getAttribute(ALTITUDE_MIN_TAG, 0);
            altitude[1] = xr.getAttribute(ALTITUDE_MAX_TAG, 0);
            xr.closeTag(GEN_TAG);

        } else if (PRODUCTION_TAG.equals(tag)
            && xr.getAttribute(DELETE_TAG, false)) {
            productionTypes.clear();
            xr.closeTag(PRODUCTION_TAG);

        } else if (PRODUCTION_TAG.equals(tag)
            && xr.getAttribute(GOODS_TYPE_TAG, (String)null) == null) {
            // new production style
            addProductionType(new ProductionType(xr, spec));

        } else if (PRODUCTION_TAG.equals(tag)
            // @compat 0.10.6
            || PRIMARY_PRODUCTION_TAG.equals(tag)
            || SECONDARY_PRODUCTION_TAG.equals(tag)) {
            GoodsType type = xr.getType(spec, GOODS_TYPE_TAG,
                                        GoodsType.class, (GoodsType)null);
            int amount = xr.getAttribute(VALUE_TAG, 0);
            AbstractGoods goods = new AbstractGoods(type, amount);
            String tileProduction = xr.getAttribute(TILE_PRODUCTION_TAG,
                                                    (String)null);
            // CAUTION: this only works if the primary production is
            // defined before the secondary production
            if (PRIMARY_PRODUCTION_TAG.equals(tag)) {
                addProductionType(new ProductionType(goods, true,
                                                     tileProduction));
            } else if (SECONDARY_PRODUCTION_TAG.equals(tag)) {
                for (ProductionType productionType : getProductionTypes(true)) {
                    if (tileProduction == null
                        || tileProduction.equals(productionType.getProductionLevel())) {
                        productionType.getOutputs().add(goods);
                    }
                }
            // end @compat
            } else {
                addProductionType(new ProductionType(goods, false,
                                                     tileProduction));
            }
            xr.closeTag(tag);

        } else if (RESOURCE_TAG.equals(tag)) {
            addResourceType(xr.getType(spec, TYPE_TAG, ResourceType.class,
                                       (ResourceType)null),
                            xr.getAttribute(PROBABILITY_TAG, 100));
            xr.closeTag(RESOURCE_TAG);

        } else if (Modifier.getXMLElementTagName().equals(tag)) {
            // @compat 0.10.7
            // the tile type no longer contains the base production modifier
            String id = xr.getAttribute(ID_ATTRIBUTE_TAG, null);
            if (id.startsWith("model.goods.")) {
                xr.closeTag(Modifier.getXMLElementTagName());
            } else {
                super.readChild(xr);
            }
            // end @compat
        } else {
            super.readChild(xr);
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getXMLTagName() { return getXMLElementTagName(); }

    /**
     * Gets the tag name of the root element representing this object.
     *
     * @return "tile-type".
     */
    public static String getXMLElementTagName() {
        return "tile-type";
    }
}
