package unbboolean.solids;

import java.util.ArrayList;
import javax.vecmath.Matrix4d;

import unbboolean.gui.solidpanels.InvalidBooleanOperationException;
import unbboolean.j3dbool.BooleanModeller;
import unbboolean.j3dbool.Solid;

/**
 * Class representing a compound solid 
 * 
 * @author Danilo Balby Silva Castanheira(danbalby@yahoo.com)
 */
public class CompoundSolid extends CSGSolid 
{
	/** union operation */
	public static final int UNION = 1;
	/** intersection operation */
	public static final int INTERSECTION = 2;
	/** difference operation */
	public static final int DIFFERENCE = 3;
	
	/** operation applied onto the operators */
	private int operation;
	/** first operator */
	private CSGSolid operator1;
	/** second operator */
	private CSGSolid operator2; 
	
	/**
	 * Constructs a customized CompoundSolid object
	 * 
	 * @param name solid name
	 * @param operation operation applied onto the operators - UNION, INTERSECTION or DIFFERENCE
	 * @param operator1 first operator
	 * @param operator2 second operator
	 * @throws InvalidBooleanOperationException if a boolean operation generates an empty solid
	 */		
	public CompoundSolid(String name, int operation, CSGSolid operator1, CSGSolid operator2) throws InvalidBooleanOperationException
	{
		super();
		this.name = name;
		this.operation = operation;
		this.operator1 = operator1;
		this.operator2 = operator2;
		
		try
		{
			applyBooleanOperation();
			operator1.setParentSolid(this);
			operator2.setParentSolid(this);
		}
		catch(InvalidBooleanOperationException e)
		{
			throw e;
		}
	}
	
   	/**
	 * String representation of a compound solid (to be used on the CSG Tree)
	 * 
	 * @return string representation
	 */
	public String toString()
	{
		if(operation==UNION)
		{
			return "U";
		}
		else if(operation==INTERSECTION)
		{
			return "\u2229";
		}
		else
		{
			return "-";			
		}
	}
	
	/**
	 * Gets the operation
	 * 
	 * @return operation
	 */ 
	public int getOperation()
	{
		return operation;
	}

	/**
	 * Gets the first operator
	 * 
	 * @return first operator
	 */ 
	public CSGSolid getOperator1()
	{
		return operator1;
	}

	/**
	 * Gets the second operator
	 * 
	 * @return second operator
	 */ 
	public CSGSolid getOperator2()
	{
		return operator2;
	}
	
	/**
	 * Sets the operation
	 * 
	 * @param operation operation
	 * @throws InvalidBooleanOperationException if a boolean operation generates an empty solid
	 */ 
	public void setOperation(int operation) throws InvalidBooleanOperationException
	{
		this.operation = operation;
		updateItselfAndParents();
	}
	
	/** 
	 * Sets the operation to inverse difference (invert the operators and apply difference) 
	 * 
	 * @throws InvalidBooleanOperationException if a boolean operation generates an empty solid
	 * */
	public void setOperationToInverseDifference() throws InvalidBooleanOperationException
	{
		this.operation = DIFFERENCE;
		CSGSolid temp = operator1;
		operator1 = operator2;
		operator2 = temp;
		updateItselfAndParents();		
	}
	
	/**
	 * Sets the first operator
	 * 
	 * @param solid first operator
	 * @throws InvalidBooleanOperationException if a boolean operation generates an empty solid
	 */ 
	public void setOperator1(CSGSolid solid) throws InvalidBooleanOperationException
	{
		operator1 = solid;
		solid.setParentSolid(this);
		updateItselfAndParents();
	}
	
	/**
	 * Sets the second operator
	 * 
	 * @param solid second operator
	 * @throws InvalidBooleanOperationException if a boolean operation generates an empty solid
	 */ 
	public void setOperator2(CSGSolid solid) throws InvalidBooleanOperationException
	{
		operator2 = solid;
		solid.setParentSolid(this);
		updateItselfAndParents();
	}
	
	/** 
	 * Update itself and parents (called when coordinates were changed)
	 * 
	 * @throws InvalidBooleanOperationException if a boolean operation generates an empty solid 
	 * */
	public void updateItselfAndParents() throws InvalidBooleanOperationException 
	{
		applyBooleanOperation();
		updateParents();
	}
	
	/** Updates all its descendants (called after some transforms were performed) */	
	public void updateChildren()
	{
		if(!(transformMatrix.equals(new Matrix4d()))) 
		{
			CSGSolid solid;
			CompoundSolid compoundSolid;
			ArrayList descendants = new ArrayList();
			descendants.add(operator1);
			descendants.add(operator2);
					
			while(!descendants.isEmpty())
			{
				solid = (CSGSolid)descendants.remove(0);
				solid.updateLocation(transformMatrix);
				if(solid instanceof CompoundSolid)
				{
					compoundSolid = (CompoundSolid)solid;
					descendants.add(compoundSolid.operator1);						
					descendants.add(compoundSolid.operator2);
				}
			}
			
			transformMatrix = startTransformMatrix();
		}
	}
	
	/** 
	 * Apply boolean operation taking as account the operation and operators set before 
	 * 
	 * @throws InvalidBooleanOperationException if a boolean operation generates an empty solid
	 * */
	private void applyBooleanOperation() throws InvalidBooleanOperationException
	{
		BooleanModeller modeller = new BooleanModeller(operator1, operator2);
		
		Solid solid;
		if(operation==CompoundSolid.UNION)
		{
			solid = modeller.getUnion();
		}
		else if(operation==CompoundSolid.INTERSECTION)
		{
			solid = modeller.getIntersection();
		}
		else
		{
			solid = modeller.getDifference();
		}
		
		if(solid.isEmpty())
		{
			throw new InvalidBooleanOperationException();
		}
		else
		{
			setData(solid.getVertices(),solid.getIndices(), solid.getColors());
		}		
	}

	/**
	 * Copies the solid
	 * 
	 * @return solid copy
	 */
	public CSGSolid copy()
	{
		try
		{
			CompoundSolid clone = new CompoundSolid(name, operation, operator1.copy(), operator2.copy());
			return clone;
		}
		catch(InvalidBooleanOperationException e)
		{
			return null;
		}
	}
}