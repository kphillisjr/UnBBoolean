package unbboolean.gui.save;

import unbboolean.gui.solidpanels.InvalidBooleanOperationException;
import unbboolean.solids.CSGSolid;
import unbboolean.solids.CompoundSolid;

/**
 * Class representing a compound solid to be saved
 * 
 * @author Danilo Balby Silva Castanheira(danbalby@yahoo.com)
 */
public class SaveCompoundSolid extends SaveSolid
{
	/** operation applied */
	private int operation;
	/** first solid operator */
	private SaveSolid operator1;
	/** second solid operator */
	private SaveSolid operator2;
	
	/**
	 * Constructs a SaveCompoundSolid object based on a CompoundSolid object
	 * 
	 * @param solid compound solid to be saved
	 */
	public SaveCompoundSolid(CompoundSolid solid)
	{  
		super(solid); 
		operation = solid.getOperation();
		operator1 = getSaveSolid(solid.getOperator1());
		operator2 = getSaveSolid(solid.getOperator2());
	}
	
	/**
	 * Gets the solid corresponding to this save solid
	 * 
	 * @return the solid corresponding to this save solid
	 */
	public CSGSolid getSolid()
	{
		try
		{
			return new CompoundSolid(name, operation, operator1.getSolid(), operator2.getSolid());
		}
		catch(InvalidBooleanOperationException e)
		{
			return null;
		}
	}
}
