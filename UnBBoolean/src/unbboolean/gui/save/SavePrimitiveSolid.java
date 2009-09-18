package unbboolean.gui.save;

import javax.vecmath.Color3f;
import javax.vecmath.Matrix4d;

import unbboolean.solids.PrimitiveSolid;

/**
 * Class representing a primitive solid to be saved
 * 
 * @author Danilo Balby Silva Castanheira(danbalby@yahoo.com)
 */
public abstract class SavePrimitiveSolid extends SaveSolid
{
	/** primitive location */
	protected Matrix4d transformMatrix;
	/** primitive color */
	protected Color3f color;
	
	/**
	 * Constructs a SavePrimitiveSolid object based on a PrimitiveSolid object
	 * 
	 * @param solid primitive solid to be saved
	 */
	public SavePrimitiveSolid(PrimitiveSolid solid)
	{
		super(solid);
		transformMatrix = solid.getLocation();
		color = solid.getColor();
	}
}
