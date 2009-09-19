package unbboolean.solids;

import javax.media.j3d.Appearance;
import javax.media.j3d.Material;
import javax.vecmath.Color3f;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import unbboolean.gui.solidpanels.InvalidBooleanOperationException;
import unbboolean.j3dbool.Solid;

/**
 * Solid class representing a component of a CSG Tree
 * 
 * @author Danilo Balby Silva Castanheira(danbalby@yahoo.com)
 */
public abstract class CSGSolid extends Solid 
{
	/** solid name */
	protected String name;
	/** matrix representing the solid position */
	protected Matrix4d transformMatrix;
	/** parent on a CSGTree */
	protected CompoundSolid parent;
	
	/** Constructs a default CSGSolid */
	public CSGSolid()
	{
		super();
		
		name = "solid";
		transformMatrix = startTransformMatrix();
		parent = null;
		defineAppearance();
	}
	
	//----------------------------------------GETS-----------------------------------//
	
	/**
	 * Gets the solid name
	 * 
	 * @return solid name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Gets the solid parent
	 * 
	 * @return solid perent
	 */
	public CompoundSolid getParentSolid()
	{
		return parent;
	}
	
	/**
	 * Gets the solid location
	 * 
	 * @return solid location
	 */
	public Matrix4d getLocation()
	{
		return (Matrix4d)transformMatrix.clone();
	}
	
	//----------------------------------------SETS-----------------------------------//
	
	/**
	 * Sets the solid name
	 * 
	 * @param name solid name
	 */
	public void setName(String name)
	{
		this.name = name;
	}
	
	/**
	 * Sets the solid parent
	 * 
	 * @param parent solid parent
	 */
	public void setParentSolid(CompoundSolid parent)
	{
		this.parent = parent;
	}
		
	//-------------------------GEOMETRICAL_TRANSFORMATIONS-------------------------//
	
	/**
	 * Applies a translation into a solid
	 * 
	 * @param dx translation on the x axis
	 * @param dy translation on the y axis
	 */
	public void translate(double dx, double dy)
	{
		if(dx!=0||dy!=0)
		{
			super.translate(dx,dy);
			
			Matrix4d matrix = startTransformMatrix();
			matrix.setTranslation(new Vector3d(dx,dy,0));
			transformMatrix.mul(matrix, transformMatrix);
		}
	}
	
	/**
	 * Applies a rotation into a solid
	 * 
	 * @param dx rotation on the x axis
	 * @param dy rotation on the y axis
	 */
	public void rotate(double dx, double dy)
	{
		double cosX = Math.cos(dx);
		double cosY = Math.cos(dy);
		double sinX = Math.sin(dx);
		double sinY = Math.sin(dy);
					
		if(dx!=0||dy!=0)
		{
			//get mean
			Point3d mean = getMean();
			
			Matrix4d matrix = startTransformMatrix();
			matrix.setTranslation(new Vector3d(new Vector3d(-mean.x, -mean.y, -mean.z)));
			transformMatrix.mul(matrix, transformMatrix);
			
			if(dx!=0)
			{
				matrix = startTransformMatrix();
				matrix.rotX(dx);
				transformMatrix.mul(matrix, transformMatrix);
			}
			if(dy!=0)
			{
				matrix = startTransformMatrix();
				matrix.rotY(dy);
				transformMatrix.mul(matrix, transformMatrix);
			}
			
			matrix = startTransformMatrix();
			matrix.setTranslation(new Vector3d(new Vector3d(mean.x, mean.y, mean.z)));
			transformMatrix.mul(matrix, transformMatrix);
		
			double newX, newY, newZ;
			Matrix4d matrix1, matrix2;	
			for(int i=0;i<vertices.length;i++)
			{
				vertices[i].x -= mean.x; 
				vertices[i].y -= mean.y; 
				vertices[i].z -= mean.z; 
				
				//x rotation
				if(dx!=0)
				{
					newY = vertices[i].y*cosX - vertices[i].z*sinX;
					newZ = vertices[i].y*sinX + vertices[i].z*cosX;
					vertices[i].y = newY;
					vertices[i].z = newZ;
				}
				
				//y rotation
				if(dy!=0)
				{
					newX = vertices[i].x*cosY + vertices[i].z*sinY;
					newZ = -vertices[i].x*sinY + vertices[i].z*cosY;
					vertices[i].x = newX;
					vertices[i].z = newZ;
				}
										
				vertices[i].x += mean.x; 
				vertices[i].y += mean.y; 
				vertices[i].z += mean.z;
			}
		}
		
		defineGeometry();
	}
	
	/**
	 * Applies a zoom into a solid
	 * 
	 * @param dz translation on the z axis
	 */
	public void zoom(double dz)
	{
		if(dz!=0)
		{
			super.zoom(dz);
			
			Matrix4d matrix = startTransformMatrix();
			matrix.setTranslation(new Vector3d(0,0,dz));
			transformMatrix.mul(matrix, transformMatrix);
		}
	}
	
	//------------------------------------LOCATION------------------------------------//
	
	/**
	 * Updates the location
	 * 
	 * @param transform matrix representing all the transformations to reach the desired position 
	 */
	public void updateLocation(Matrix4d transform)
	{
		for(int i=0;i<vertices.length;i++)
		{
			double x = transform.m00*vertices[i].x + transform.m01*vertices[i].y + transform.m02*vertices[i].z + transform.m03;		
			double y = transform.m10*vertices[i].x + transform.m11*vertices[i].y + transform.m12*vertices[i].z + transform.m13;
			double z = transform.m20*vertices[i].x + transform.m21*vertices[i].y + transform.m22*vertices[i].z + transform.m23;
			
			vertices[i].x = x;		
			vertices[i].y = y;
			vertices[i].z = z;			
		}
		
		transformMatrix.mul(transform, transformMatrix);
				
		defineGeometry();
	}
	
	/** Updates the parent location - called when the the coordinates were changed */
	public void updateParents() throws InvalidBooleanOperationException
	{
		if(parent!=null)
		{
			parent.updateItselfAndParents();
		}
	}

	
	//------------------------------------OTHERS---------------------------------//
	
	/** Light the solid */
	public void light()
	{
		Appearance appearance = new Appearance();
		appearance.setCapability(Appearance.ALLOW_MATERIAL_READ);
		
		//PolygonAttributes polygonAtt = new PolygonAttributes();
		//polygonAtt.setCullFace(PolygonAttributes.CULL_NONE);
		//polygonAtt.setPolygonMode(PolygonAttributes.POLYGON_LINE);
		//polygonAtt.setBackFaceNormalFlip(true);
		//appearance.setPolygonAttributes(polygonAtt);
		
		Material material = new Material();
		material.setCapability(Material.ALLOW_COMPONENT_READ);
		material.setDiffuseColor(1,1,1);
		material.setAmbientColor(1,1,1);
		material.setSpecularColor(0.0f, 0.0f, 0.0f);
		appearance.setMaterial(material);
		
		setAppearance(appearance);
	}
	
	/** Unight the solid */
	public void unlight()
	{
		defineAppearance();
	}
	
	/** 
	 * Checks if this solid is lighten 
	 *
	 * @return true if the solid is lighten, false otherwise 
	 */
	public boolean isLighted()
	{
		Color3f diffuseColor = new Color3f();
		getAppearance().getMaterial().getDiffuseColor(diffuseColor);
		if(diffuseColor.equals(new Color3f(1,1,1)))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	//----------------------------------PRIVATES-------------------------------------//
	
	/** Creates an appearance for the solid */
	protected void defineAppearance()
	{
		Appearance appearance = new Appearance();
		appearance.setCapability(Appearance.ALLOW_MATERIAL_READ);
		
		//PolygonAttributes polygonAtt = new PolygonAttributes();
		//polygonAtt.setCullFace(PolygonAttributes.CULL_NONE);
		//polygonAtt.setPolygonMode(PolygonAttributes.POLYGON_LINE);
		//polygonAtt.setBackFaceNormalFlip(true);
		//appearance.setPolygonAttributes(polygonAtt);
		
		Material material = new Material();
		material.setCapability(Material.ALLOW_COMPONENT_READ);
		material.setDiffuseColor(0.3f, 0.3f, 0.3f);
		material.setAmbientColor(0.3f, 0.3f, 0.3f);
		material.setSpecularColor(0.0f, 0.0f, 0.0f);
		appearance.setMaterial(material);
		
		setAppearance(appearance);
	}
	
	/**
	 * start transform matrix as an identity matrix
	 * 
	 * @return identity matrix
	 */
	protected Matrix4d startTransformMatrix()
	{
		Matrix4d  matrix = new Matrix4d();
		matrix.m00 = 1;
		matrix.m11 = 1;
		matrix.m22 = 1;
		matrix.m33 = 1;
		return matrix;
	}

	//----------------------------------UNIMPLEMENTED--------------------------------//

	/**
	 * Copies the solid
	 * 
	 * @return solid copy
	 */
	public abstract CSGSolid copy();

}