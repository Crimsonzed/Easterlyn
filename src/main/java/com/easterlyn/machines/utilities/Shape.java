package com.easterlyn.machines.utilities;

import org.bukkit.Axis;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Orientable;
import org.bukkit.block.data.Rotatable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

/**
 * A blank structure for building multi-block Machines.
 *
 * @author Jikoo
 */
public class Shape {

	public class MaterialDataValue {
		Material material;
		Direction direction;

		public MaterialDataValue(Material material, Direction direction) {
			this.material = material;
			this.direction = direction;
		}

		public MaterialDataValue(Material material) {
			this.material = material;
		}

		public  Material getMaterial() {
			return this.material;
		}

		public Direction getDirection() {
			return this.direction;
		}

		public void build(Block block) {
			block.setType(this.material, false);
			if (this.direction == null) {
				return;
			}

			BlockData data = block.getBlockData();
			boolean dirty = false;

			if (data instanceof Bisected) {
				((Bisected) data).setHalf(this.direction == Direction.UP ? Bisected.Half.TOP : Bisected.Half.BOTTOM);
				dirty = true;
			}

			if (data instanceof Directional) {
				Directional directional = (Directional) data;
				BlockFace faceDirection = this.direction.toBlockFace();
				if (!directional.getFaces().contains(faceDirection)) {
					System.err.println(String.format("Invalid facing %s for material %s. Valid faces: %s",
							faceDirection.name(), this.material.name(), directional.getFaces()));
					faceDirection = directional.getFaces().iterator().next();
				}
				directional.setFacing(faceDirection);
				dirty = true;
			}

			if (data instanceof Orientable) {
				Orientable orientable = (Orientable) data;
				Axis axis = this.direction.toAxis();
				if (!orientable.getAxes().contains(axis)) {
					System.err.println(String.format("Invalid axis %s for material %s. Valid axes: %s",
							axis.name(), this.material.name(), orientable.getAxes()));
					axis = orientable.getAxes().iterator().next();
				}
				orientable.setAxis(axis);
				dirty = true;
			}

			if (data instanceof Rotatable) {
				((Rotatable) data).setRotation(this.direction.toBlockFace());
				dirty = true;
			}

			// TODO: rework to support multiple settings (? extends BlockData type, T... value)
			// I.E. Stairs: Directional and Bisected
			if (dirty) {
				block.setBlockData(data, false);
			}

		}

	}

	/** All relative Locations and Materials of the Machine. */
	private final LinkedHashMap<Vector, MaterialDataValue> vectors;

	/**
	 * Constructor of Shape. Creates a blank Shape.
	 */
	public Shape() {
		this.vectors = new LinkedHashMap<>();
	}

	/**
	 * Sets the MaterialData for a Vector.
	 *
	 * @param vector the Vector
	 * @param data the MaterialData
	 */
	public void setVectorData(Vector vector, MaterialDataValue data) {
		vectors.put(vector, data);
	}

	/**
	 * Sets the MaterialData for a Vector.
	 *
	 * @param vector the Vector
	 * @param data the MaterialData
	 */
	public void setVectorData(Vector vector, Material data, Direction rotation) {
		setVectorData(vector, new MaterialDataValue(data, rotation));
	}

	/**
	 * Sets the MaterialData for a Vector.
	 *
	 * @param vector the Vector
	 */
	public void setVectorData(Vector vector, Material data) {
		setVectorData(vector, new MaterialDataValue(data));
	}

	/**
	 * Gets a HashMap of all properly oriented Locations and Materials needed to
	 * build a Machine.
	 *
	 * @param location the Location to center the Shape on
	 * @param direction the Direction the Machine needs to be built in
	 *
	 * @return the Locations and relative MaterialData
	 */
	public HashMap<Location, MaterialDataValue> getBuildLocations(Location location, Direction direction) {
		HashMap<Location, MaterialDataValue> newLocs = new HashMap<>();
		for (Entry<Vector, MaterialDataValue> entry : vectors.entrySet()) {
			newLocs.put(location.clone().add(getRelativeVector(direction, entry.getKey().clone())), new MaterialDataValue(entry.getValue().material, entry.getValue().direction == null ? null : direction.getRelativeDirection(entry.getValue().direction)));
		}
		return newLocs;
	}

	public void build(Location location, Direction direction) {
		for (Entry<Location, MaterialDataValue> entry : this.getBuildLocations(location, direction).entrySet()) {
			entry.getValue().build(entry.getKey().getBlock());
		}
	}

	/**
	 * Gets a Vector translated from the internal Easterlyn representation.
	 *
	 * Internally, we consider north to be positive Z, east to be positive X. In Minecraft, north is negative Z.
	 * @param direction the Direction
	 * @param vector the Vector to translate
	 * @return the new Vector
	 */
	public static Vector getRelativeVector(Direction direction, Vector vector) {
		switch (direction) {
		case EAST:
			double newZ = vector.getX();
			vector.setX(vector.getZ());
			vector.setZ(newZ);
			return vector;
		case SOUTH:
			vector.setX(-2 * vector.getBlockX() + vector.getX());
			int blockZ = (int) vector.getZ();
			vector.setZ(blockZ + vector.getZ() - blockZ);
			return vector;
		case WEST:
			double newZ1 = -2 * vector.getBlockX() + vector.getX();
			vector.setX(-2 * vector.getBlockZ() + vector.getZ());
			vector.setZ(newZ1);
			return vector;
		case NORTH:
		default:
			vector.setZ(-2 * vector.getBlockZ() + vector.getZ());
			return vector;
		}
	}

}
