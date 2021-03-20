package net.runelite.client.plugins.gpu;

import com.google.inject.Inject;
import com.jogamp.opengl.math.Matrix4;
import com.jogamp.opengl.math.VectorUtil;
import com.jogamp.opengl.math.geom.Frustum;
import static com.jogamp.opengl.math.geom.Frustum.BOTTOM;
import static com.jogamp.opengl.math.geom.Frustum.FAR;
import static com.jogamp.opengl.math.geom.Frustum.LEFT;
import static com.jogamp.opengl.math.geom.Frustum.NEAR;
import static com.jogamp.opengl.math.geom.Frustum.RIGHT;
import static com.jogamp.opengl.math.geom.Frustum.TOP;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Player;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.Overlay;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class DebugOverlay extends Overlay
{
	@Inject
	private Client client;

	@javax.inject.Inject
	private GpuPluginConfig config;

	private int w = 200;
	private int h = w;
	private int basePointSize = 100;

	public double shadowPitch = 0;
	public double shadowYaw = 0;
	public Matrix4 perspective = new Matrix4();

	Color[] colors = new Color[]{
		Color.RED,
		Color.GREEN,
		Color.BLUE,
		Color.BLACK,
		Color.GRAY,
		Color.WHITE,
		Color.CYAN,
		Color.MAGENTA,
		Color.YELLOW
	};

	private void drawPoint(Graphics2D g, float[] vec4)
	{
		float pSize = basePointSize / (float) w;
		float z = (1.f - vec4[2]) * 1000;
		pSize *= z;
		g.fillOval(
			Math.round((1 + vec4[0]) / 2.f * w - pSize / 2.f),
			Math.round((1 - (1 + vec4[1]) / 2.f) * h - pSize / 2.f),
			Math.round(pSize), Math.round(pSize));
	}

	private int[][] getLocalBounds()
	{
		// TODO: Z bounds could be improved by tracking min and max height while uploading tiles and models
		// TODO: Could swap Y and Z here to avoid having to swap back and forth for calculations
		return new int[][]
			{
				{ 128,  13056 },
				{ 128,  13056 },
				{   0, Integer.MIN_VALUE }
			};
	}

	public Matrix4 getFittedOrthographicProjection(Matrix4 perspective, boolean infinite)
	{
		float[][] bounds = getFrustumBounds(perspective);

		float left = bounds[1][0]; // top left X
		float right = bounds[2][0]; // top right X
		float bottom = bounds[0][2]; // bottom left Y
		float top = bounds[1][2]; // top left Y
		float near = bounds[1][1]; // top left Z
		float far = bounds[0][1]; // bottom left Z

		// Override near and far since they still often give zero width
		near = -1000;
		far = 20000;

		Matrix4 ortho = new Matrix4();

		float dx = right - left;
		float dy = top - bottom;
		float dz = far - near;

		if (infinite)
		{
			ortho.multMatrix(new float[]
				{
					// Infinite range, but as a consequence squeezes depth information to zero
					2.f / dx, 0, 0, 0,
					0, 2.f / dy, 0, 0,
					0, 0, 0, 0,
					0, 0, 0, 1
				});
		}
		else
		{
			ortho.multMatrix(new float[]
				{
					2.f / dx, 0, 0, 0,
					0, 2.f / dy, 0, 0,
					0, 0, 2 / (near - far), 0,
					0, 0, near / (near - far), 1
				});
		}


		ortho.rotate((float) (Math.PI - shadowPitch), -1, 0, 0);
		ortho.rotate((float) (shadowYaw), 0, 1, 0);
//		ortho.translate(-client.getCameraX2(), -client.getCameraY2(), -client.getCameraZ2());
		ortho.translate(-client.getCameraX2(), 0, -client.getCameraZ2());
//		ortho.translate(-dx / 2.f, 0, 0);
//		ortho.translate(-dx / 2.f, -dy / 2.f, 0);

//		print(new float[] {dx, dy, dz});
//		print(new float[] {left, right, bottom, top, near, far});

		return ortho;
	}

//	public Matrix4 getFittedOrthographicProjection(Matrix4 perspective, boolean infinite)
//	{
//		float[][] bounds = getFrustumBounds(perspective);
//
//		float left = bounds[1][0]; // top left X
//		float right = bounds[2][0]; // top right X
//		float bottom = bounds[0][2]; // bottom left Y
//		float top = bounds[1][2]; // top left Y
//		float near = bounds[1][1]; // top left Z
//		float far = bounds[0][1]; // bottom left Z
//
//		// Override near and far since they still often give zero width
////		near = -1000;
////		far = 20000;
//
//		Matrix4 ortho = new Matrix4();
//
//		float dx = right - left;
//		float dy = top - bottom;
//		float dz = far - near;
//
//		if (infinite)
//		{
//			ortho.multMatrix(new float[]
//				{
//					// Infinite range, but as a consequence squeezes depth information to zero
//					2.f / dx, 0, 0, 0,
//					0, 2.f / dy, 0, 0,
//					0, 0, 0, 0,
//					0, 0, 0, 1
//				});
//		}
//		else
//		{
//			ortho.multMatrix(new float[]
//				{
//					2.f / dx, 0, 0, 0,
//					0, 2.f / dy, 0, 0,
//					0, 0, 2 / (near - far), 0,
//					0, 0, near / (near - far), 1
//				});
//		}
//
//
//		ortho.rotate((float) (Math.PI - shadowPitch), -1, 0, 0);
//		ortho.rotate((float) (shadowYaw), 0, 1, 0);
////		ortho.translate(-client.getCameraX2(), -client.getCameraY2(), -client.getCameraZ2());
//		ortho.translate(-client.getCameraX2(), 0, -client.getCameraZ2());
////		ortho.translate(-dx / 2.f, 0, 0);
////		ortho.translate(-dx / 2.f, -dy / 2.f, 0);
//
////		print(new float[] {dx, dy, dz});
////		print(new float[] {left, right, bottom, top, near, far});
//
//		return ortho;
//	}

	@Override
	public Dimension render(Graphics2D g)
	{
		if (!config.enableDebugMode())
			return null;

		w = 200;
		h = 200;
//		w = client.getViewportWidth();
//		h = client.getViewportHeight();

		// Draw outline
		g.setColor(new Color(1.f, 1.f, 1.f, .5f));
		g.drawRect(0, 0, w, h);

		visualizeCornerBoundsIntersections(g);

		g.setColor(Color.GREEN);
		visualizeFrustumBounds(g);

		g.setColor(Color.RED);
		visualizeFrustumPlane(g);

		// TODO: the now static scene bounds could be used to control shadow distance

//		Matrix4 ortho = getFittedOrthographicProjection(perspective, false);
//
//		float[][] bounds = getFrustumBounds(perspective);
////		System.out.println(Arrays.deepToString(bounds));
//		for (int i = 0; i < bounds.length; i++)
//		{
//			float[] result = new float[4];
//			ortho.multVec(bounds[i], result);
//			perspectiveDivide(result);
//			swapYZ(result);
//			bounds[i] = result;
//		}
//
//		g.setColor(Color.RED);
//		float[] offset = new float[] {1, 1};
//		float[] scale = new float[] {w / 2.f, h / 2.f};
//		for (float[] coord : bounds)
//		{
//			VectorUtil.addVec2(coord, coord, offset);
//			VectorUtil.scaleVec2(coord, coord, scale);
//		}
//
//		double threshold = 1e7;
//
//		outer:
//		for (int i = 1; i <= bounds.length; i++)
//		{
//			float[] from = bounds[i-1];
//			float[] to = bounds[i % bounds.length];
//			for (float v : from)
//				if (Float.isNaN(v) || Math.abs(v) > threshold)
//					continue outer;
//			for (float v : to)
//				if (Float.isNaN(v) || Math.abs(v) > threshold)
//					continue outer;
//			g.drawLine((int) from[0], (int) from[1], (int) to[0], (int) to[1]);
//		}

		return new Dimension(w, h);
	}

	public Matrix4 getOrthographicProjection(Matrix4 projection)
	{
		Matrix4 ortho = new Matrix4();

		Player player = client.getLocalPlayer();
		if (player == null)
			return ortho;
		LocalPoint loc = player.getLocalLocation();
		if (loc == null)
			return ortho;

		FrustumPlane plane = getFrustumPlane(projection, true);

		float dx = plane.calculateWidth();
		float dy = plane.calculateHeight();

//		print("dx dy: ", new float[] {dx, dy});

		// Infinite range, but as a consequence squeezes depth information to zero. Useful while debugging
//		ortho.multMatrix(new float[]
//			{
//				2.f / dx, 0, 0, 0,
//				0, 2.f / dy, 0, 0,
//				0, 0, 0, 0,
//				0, 0, 0, 1
//			});

		// TODO: far and near clipping planes could be improved
		float near = 3000;
		float far = -2000;
		// Infinite range with depth axis flipped so depth information is retained
		ortho.multMatrix(new float[]
			{
				2.f / dx, 0, 0, 0,
				0, 2.f / dy, 0, 0,
				0, 0, -.0001f, 0,
				0, 0, 0, 1
			});
//		ortho.multMatrix(new float[]
//			{
//				2.f / dx, 0, 0, 0,
//				0, -2.f / dy, 0, 0,
//				0, 0, 2.f / (near - far), 0,
//				0, 0, near / (near - far), 1
//			});

		// TODO: Correct for missing depth info if needed

		// TODO: Rescale to stretch what's visible to the full viewport

		// Prior to alignment and projection, Y is depth, Z is up
		// We want to end up with Y as up and Z as depth in screen space
		// (actually we want the direction the plane's normal is pointing to be depth)

		// Apply shadow rotations after aligning with plane normal
		// (yaw, then pitch, everything's in reverse order)
		ortho.rotate((float) (Math.PI - shadowPitch), -1, 0, 0);
		ortho.rotate((float) shadowYaw, 0, 1, 0);

		// Align viewport with plane normal
		// Rotate around X axis by angle between plane normal and -Z/down axis to match pitch of plane normal
		// plane pitch 0 = side-view looking north, pitch 90 degrees = looking down at the world
//		float planePitch = VectorUtil.angleVec3(planeNormal, new float[] {0, 0, -1});
//		ortho.rotate(planePitch, -1, 0, 0);

		// Rotate around Z axis by angle between plane normal and Y/north/inward/forward axis to match yaw of plane normal
		// This rotation happens prior to pitch rotation (everything's in reverse order)
//		float planeYaw = VectorUtil.angleVec3(planeNormal, new float[] {0, 1, 0});
//		ortho.rotate(planeYaw, 0, 1, 0);
		// TODO: plane yaw still feels very weird

		// Rotate around Z axis by angle between plane normal and Y/north/inward/forward axis to match yaw of plane normal
		// This rotation happens prior to pitch rotation (everything's in reverse order)
//		float planeTilt = VectorUtil.angleVec3(planeNormal, new float[] {-1, 0, 0});
//		ortho.rotate(planeYaw, 0, 0, -1);
		// TODO: plane yaw still feels very weird

		// Shift world origin to the center of the plane
		centerMatrix(ortho, plane.origin);

//		print(ortho.getMatrix());

		return ortho;
	}

	// Uses bounds intersection points in world space to determine orthographic projection
//	public Matrix4 getOrthographicProjection(Matrix4 projection)
//	{
//		Matrix4 ortho = new Matrix4();
//
//		Player player = client.getLocalPlayer();
//		if (player == null)
//			return ortho;
//		LocalPoint loc = player.getLocalLocation();
//		if (loc == null)
//			return ortho;
//
//		float[][] frustumPlane = getFrustumPlane(projection, true);
//		float[] planeOrigin = frustumPlane[0];
//		float[] planeNormal = frustumPlane[1];
//		float[] planeVecDown = frustumPlane[2];
//		float[] planeVecRight = frustumPlane[3];
//
//		float dx = vectorLength(planeVecRight) * 2;
//		float dy = vectorLength(planeVecDown) * 2;
//
////		print("d: ", new float[] {dx, dy});
//
//		// Infinite range, but as a consequence squeezes depth information to zero. Useful while debugging
////		ortho.multMatrix(new float[]
////			{
////				2.f / dx, 0, 0, 0,
////				0, -2.f / dy, 0, 0,
////				0, 0, 0, 0,
////				0, 0, 0, 1
////			});
//
//		// TODO: far and near clipping planes could be improved
//		float near = 3000;
//		float far = -2000;
//		// Infinite range with depth axis flipped so depth information is retained
//		ortho.multMatrix(new float[]
//			{
//				2.f / dx, 0, 0, 0,
//				0, -2.f / dy, 0, 0,
//				0, 0, .0001f, 0,
//				0, 0, 0, 1
//			});
////		ortho.multMatrix(new float[]
////			{
////				2.f / dx, 0, 0, 0,
////				0, -2.f / dy, 0, 0,
////				0, 0, 2.f / (near - far), 0,
////				0, 0, near / (near - far), 1
////			});
//
//		// TODO: Correct for missing depth info if needed
//
//		// TODO: Rescale to stretch what's visible to the full viewport
//
//		// Prior to alignment and projection, Y is depth, Z is up
//		// We want to end up with Y as up and Z as depth in screen space
//		// (actually we want the direction the plane's normal is pointing to be depth)
//
//		// Apply shadow rotations after aligning with plane normal
//		// (yaw, then pitch, everything's in reverse order)
//		ortho.rotate((float) shadowPitch, 1, 0, 0);
//		ortho.rotate((float) shadowYaw, 0, 1, 0);
//
//		// Align viewport with plane normal
//		// Rotate around X axis by angle between plane normal and Z/up axis to match pitch of plane normal
//		// plane pitch 0 = side-view looking north, pitch 90 degrees = looking down at the world
//		float planePitch = VectorUtil.angleVec3(planeNormal, new float[] {0, 0, 1});
////		ortho.rotate(planePitch, 1, 0, 0);
//
//		// Rotate around Z axis by angle between plane normal and Y/north/inward/forward axis to match yaw of plane normal
//		// This rotation happens prior to pitch rotation (everything's in reverse order)
//		float planeYaw = VectorUtil.angleVec3(planeNormal, new float[] {0, 1, 0});
////		ortho.rotate(planeYaw, 0, 1, 0);
//		// TODO: plane yaw still feels very weird
//
//		// Shift world origin to the center of the plane
//		float offsetX = -planeOrigin[0];
//		float offsetY = -planeOrigin[1];
//		float offsetZ = -planeOrigin[2];
//
////		offsetX = 0;
////		offsetY = 0;
////		offsetZ = 0;
//
//		ortho.translate(offsetX, offsetY, offsetZ);
//
////		print(ortho.getMatrix());
//
//		return ortho;
//	}

	public float[] calculateVector(float[] from, float[] to)
	{
		float[] v = new float[4];
		VectorUtil.subVec3(v, to, from);
		return v;
	}

	public class FrustumPlane
	{
		public float[] origin, normal, topLeft, topRight, bottomRight, bottomLeft;
		public float[][] corners;

		public FrustumPlane(float[] origin, float[] normal, float[][] corners)
		{
			this.origin = origin;
			this.normal = normal;
			this.corners = corners;
			this.topLeft = corners[0];
			this.topRight = corners[1];
			this.bottomRight = corners[2];
			this.bottomLeft = corners[3];
		}

		public float calculateWidth()
		{
			return Math.max(
				vectorLength(calculateVector(topLeft, topRight)),
				vectorLength(calculateVector(bottomLeft, bottomRight))
			);
		}

		public float calculateHeight()
		{
			return Math.max(
				vectorLength(calculateVector(topLeft, bottomLeft)),
				vectorLength(calculateVector(topRight, bottomRight))
			);
		}
	}

	/**
	 * Calculate a plane based on what's visible on the screen.
	 * The plane usually goes from below the world surface to the points visible at the top of the screen.
	 * @param projection The projection that should be used for casting rays through the screen's corners
	 * @param centerOrigin Whether to center the plane's origin
	 * @return float[][] { origin, normal, vecTopLeftBottomLeft, vecTopLeftTopRight, vecTopRightBottomRight, vecBottomLeftBottomRight }
	 */
	public FrustumPlane getFrustumPlane(Matrix4 projection, boolean centerOrigin)
	{
		Matrix4 inverseProjection = new Matrix4();
		inverseProjection.multMatrix(projection);
		inverseProjection.invert();

		float[][] corners = {
			{-1, 1}, // top left
			{ 1, 1}, // top right
			{ 1,-1}, // bottom right
			{-1,-1}  // bottom left
		};

		float[] camPos = {
			client.getCameraX(),
			client.getCameraY(),
			client.getCameraZ()
		};

		// Project from screen to world space
		float[] worldCenter = new float[4];
		inverseProjection.multVec(new float[] {0, 0, 0, 1}, worldCenter);
		perspectiveDivide(worldCenter);
		swapYZ(worldCenter);

		int[][] localBounds = getLocalBounds();
		float[] groundPlaneOrigin = worldCenter;
		float[] groundPlaneNormal = {0, 0, 1};

		for (int i = 0; i < corners.length; i++)
		{
			float[] screenPoint = {
				corners[i][0],
				corners[i][1],
				1, // Z = far plane
				1  // W = point at infinite distance
			};

			// Project from screen to world space
			float[] worldDirection = new float[4];
			inverseProjection.multVec(screenPoint, worldDirection);
			// Don't perspective divide since it's infinitely far away
			swapYZ(worldDirection); // Swap Y and Z to get depth/height on Z instead of Y
			// Normalization isn't strictly necessary, but without we might skip certain intersections
			VectorUtil.normalizeVec3(worldDirection);

			float[] intersection = intersectPlane(camPos, worldDirection, groundPlaneOrigin, groundPlaneNormal);
//			print("dir - intersection: " + Arrays.toString(worldDirection), intersection);
			if (intersection == null)
			{
//				print("no intersection for: ", worldDirection);
				intersection = new float[4];
				intersection[0] = worldDirection[0] < 0 ? localBounds[0][0] : localBounds[0][1];
				intersection[1] = worldDirection[1] < 0 ? localBounds[1][0] : localBounds[1][1];
//				intersection[2] = groundPlaneOrigin[2];
			}
			else
			{
				constrainPointToBounds(intersection, localBounds);
			}

			intersection[3] = 1; // Set W to 1 as normal, since we don't want to do the
			// calculation with W set to the intersection's distance multiplier
			swapYZ(intersection); // Swap back to RS coordinates where -Z is up

			corners[i] = intersection;
		}

//		print("corners: ", corners);

//		System.out.println(Arrays.deepToString(corners));

		float[] normal = {0, 0, -1, 0};
//		float[] vecRight = calculateVector(corners[0], corners[3]);; // top left -> bottom left
//		float[] vecUp = calculateVector(corners[0], corners[1]);; // top left -> top right

		float[] origin = new float[4];
		// Imprecise way when corners aren't playing nice
		VectorUtil.addVec3(origin, origin, corners[0]);
		origin[3] = 1;
//		if (centerOrigin)
//		{
//			// Halve vectors
//			VectorUtil.scaleVec3(vecRight, vecRight, .5f);
//			VectorUtil.scaleVec3(vecUp, vecUp, .5f);
//			// Reposition plane origin to the center of the plane
//			VectorUtil.addVec3(origin, origin, vecRight);
//			VectorUtil.addVec3(origin, origin, vecUp);
//		}

		if (centerOrigin)
		{
			VectorUtil.addVec3(origin, origin, corners[1]);
			VectorUtil.addVec3(origin, origin, corners[2]);
			VectorUtil.addVec3(origin, origin, corners[3]);
			VectorUtil.scaleVec3(origin, origin, .25f);
		}

		return new FrustumPlane(origin, normal, corners);
	}

//	/**
//	 * Calculate a plane based on what's visible on the screen.
//	 * The plane usually goes from below the world surface to the points visible at the top of the screen.
//	 * @param projection The projection that should be used for casting rays through the screen's corners
//	 * @param centerOrigin Whether to center the plane's origin
//	 * @return float[][] { origin, normal, vecUp, vecRight }
//	 */
//	public float[][] getFrustumPlane(Matrix4 projection, boolean centerOrigin)
//	{
//		Matrix4 inverseProjection = new Matrix4();
//		inverseProjection.multMatrix(projection);
//		inverseProjection.invert();
//
//		// bottom left, top left, bottom right
//		// works fine, but prioritizes near plane instead of far plane which makes it harder to work with
//		float[][] corners = {
//			{-1,-1},
//			{-1, 1},
//			{ 1,-1}
//		};
//
//		float[] camPos = {
//			client.getCameraX(),
//			client.getCameraY(),
//			client.getCameraZ()
//		};
//
//		for (int i = 0; i < corners.length; i++)
//		{
//			float[] screenPoint = {
//				corners[i][0],
//				corners[i][1],
//				1, // Z = far plane
//				1  // W = point at infinite distance
//			};
//
//			// Project from screen to world space
//			float[] worldPoint = new float[4];
//			inverseProjection.multVec(screenPoint, worldPoint);
//
//			// Don't perspective divide since it's infinitely far away
//			float[] dir = worldPoint;
//			swapYZ(dir); // Swap Y and Z to get depth/height on Z instead of Y
//			// Normalization isn't strictly necessary, but without we might skip certain intersections
//			VectorUtil.normalizeVec3(dir);
//
//			float[][][] intersections = getBoundsIntersections(camPos, dir);
//
//			float[] closest = getClosestIntersection(intersections);
//			if (closest != null)
//			{
//				closest[3] = 1; // Set W to 1 as normal, since we don't want to do the
//				// calculation with W set to the intersection's distance multiplier
//				swapYZ(closest); // Swap back to RS coordinates where -Z is up
//				corners[i] = closest;
////				print("intersection corner: " + i + " - ", closest);
//			}
//			else
//			{
//				corners[i] = new float[] {0, 0, 0, 1};
//				System.out.println("no intersection for corner " + Arrays.toString(corners[i]));
//			}
//		}
//
//		float[] normal = new float[3];
//		float[] vecA = new float[3]; // bottom left -> top left
//		float[] vecB = new float[3]; // bottom left -> bottom right
//		VectorUtil.subVec3(vecA, corners[1], corners[0]);
//		VectorUtil.subVec3(vecB, corners[2], corners[0]);
//
//		// Calculate normal pointing away from camera
//		VectorUtil.crossVec3(normal, vecA, vecB);
//		VectorUtil.normalizeVec3(normal);
//
//		float[] origin = corners[0];
//		if (centerOrigin)
//		{
//			// Reposition plane origin to the center of the plane
//			VectorUtil.scaleVec3(vecA, vecA, .5f);
//			VectorUtil.scaleVec3(vecB, vecB, .5f);
//			VectorUtil.addVec3(origin, origin, vecA);
//			VectorUtil.addVec3(origin, origin, vecB);
//		}
//
////		System.out.println("origin: " + Arrays.toString(origin) + ", normal: " + Arrays.toString(normal));
//
//		return new float[][] { origin, normal, vecA, vecB };
//	}

	public float[][] getFrustumBounds(Matrix4 projection)
	{
		return getFrustumBounds(projection, 1);
	}

	public float[][] getFrustumBounds(Matrix4 projection, float scale)
	{
		Matrix4 inverseProjection = new Matrix4();
		inverseProjection.multMatrix(projection);
		inverseProjection.invert();

		float[][] corners = {
			{-1,-1},
			{-1, 1},
			{ 1, 1},
			{ 1,-1}
		};

		float[] camPos = {
			client.getCameraX(),
			client.getCameraY(),
			client.getCameraZ()
		};

		for (int i = 0; i < corners.length; i++)
		{
			float[] screenPoint = {
				corners[i][0],
				corners[i][1],
				1, // Z = far plane
				1  // W = point at infinite distance
			};

			// Just used for testing
			VectorUtil.scaleVec2(screenPoint, screenPoint, scale);

			// Project from screen to world space
			float[] worldPoint = new float[4];
			inverseProjection.multVec(screenPoint, worldPoint);

			// Don't perspective divide since it's infinitely far away
			float[] dir = worldPoint;
			swapYZ(dir); // Swap Y and Z to get depth/height on Z instead of Y
			// Normalization isn't strictly necessary, but without we might skip certain intersections
			VectorUtil.normalizeVec3(dir);

			float[][][] intersections = getBoundsIntersections(camPos, dir);

			float[] closest = getClosestIntersection(intersections);
			if (closest != null)
			{
				closest[3] = 1; // Set W to 1 as normal, since we don't want to do the
				// calculation with W set to the intersection's distance multiplier
				swapYZ(closest); // Swap back to RS coordinates where -Z is up
				corners[i] = closest;
//				print("intersection corner: " + i + " - ", closest);
			}
			else
			{
				corners[i] = new float[] {0, 0, 0, 1};
				System.out.println("no intersection for corner " + Arrays.toString(corners[i]));
			}
		}

		return corners;
	}

	private void visualizeCornerBoundsIntersections(Graphics2D g)
	{
		Matrix4 projection = getPerspectiveProjection();

		Matrix4 inverseProjection = new Matrix4();
		inverseProjection.multMatrix(projection);
		inverseProjection.invert();

		for (int i = 0; i < 9; i++)
		{
//			if (i != 7)
//				continue;
			Color c = colors[i];
			c = new Color(c.getRed(), c.getGreen(), c.getBlue(), 75);
			float x = i % 3 - 1;
			float y = i / 3 - 1;
			float[] point = {x, y, 0, 0};
			g.setColor(c);
			visualiseCornerBoundsIntersection(g, projection, inverseProjection, point);
		}
	}

	private void visualizeFrustumPlane(Graphics2D g)
	{
		FrustumPlane plane = getFrustumPlane(perspective, true);

		float[][] bounds = plane.corners;
//		System.out.println(Arrays.deepToString(bounds));
		for (int i = 0; i < bounds.length; i++)
		{
			float[] result = new float[4];
			perspective.multVec(bounds[i], result);
			perspectiveDivide(result);
			bounds[i] = result;
		}

		float[] offset = new float[] {1, -1};
		float[] scale = new float[] {w / 2.f, -h / 2.f};
		for (float[] coord : bounds)
		{
			VectorUtil.addVec2(coord, coord, offset);
			VectorUtil.scaleVec2(coord, coord, scale);
		}

		double threshold = 1e9;

		outer:
		for (int i = 1; i <= bounds.length; i++)
		{
			float[] from = bounds[i-1];
			float[] to = bounds[i % bounds.length];
			for (float v : from)
				if (Float.isNaN(v) || Math.abs(v) > threshold)
					continue outer;
			for (float v : to)
				if (Float.isNaN(v) || Math.abs(v) > threshold)
					continue outer;
			g.drawLine((int) from[0], (int) from[1], (int) to[0], (int) to[1]);
		}
	}

//	private void visualizeFrustumPlane(Graphics2D g)
//	{
//		float[][] plane = getFrustumPlane(perspective, true);
//		float[] vecDownHalf = plane[2];
//		float[] vecRightHalf = plane[3];
//		float[] center = plane[0];
//		float[] vecDown = new float[3];
//		float[] vecRight = new float[3];
//
//		// Double vectors to get full edges of the plane instead of half from center
//		VectorUtil.scaleVec3(vecDown, vecDownHalf, 2);
//		VectorUtil.scaleVec3(vecRight, vecRightHalf, 2);
//
//		float[] topLeft = new float[4];
//		float[] bottomLeft = new float[4];
//		float[] topRight = new float[4];
//		float[] bottomRight = new float[4];
//
//		// Initialize W to 1, otherwise the results go haywire
//		topLeft[3] = 1;
//		bottomLeft[3] = 1;
//		topRight[3] = 1;
//		bottomRight[3] = 1;
//
//		// Move center point to top left
//		VectorUtil.subVec3(topLeft, center, vecRightHalf);
//		VectorUtil.subVec3(topLeft, center, vecDownHalf);
//
//		// Calculate other corners from bottom left and full edge vectors
//		VectorUtil.addVec3(bottomLeft, topLeft, vecDown);
//		VectorUtil.addVec3(bottomRight, bottomLeft, vecRight);
//		VectorUtil.subVec3(topRight, bottomRight, vecDown);
//
//		float[][] bounds = {
//			topLeft,
//			bottomLeft,
//			bottomRight,
//			topRight
//		};
//
////		System.out.println(Arrays.deepToString(bounds));
//
//		for (int i = 0; i < bounds.length; i++)
//		{
//			float[] result = new float[4];
//			perspective.multVec(bounds[i], result);
//			perspectiveDivide(result);
//			bounds[i] = result;
//		}
//
//
//		// Center bottom coords to bettel align the visualization to what's intuitive
//		float bottomXDiff = bounds[2][0] - bounds[1][0];
////		bounds[1][0] = 1 - bottomXDiff / 2.f;
////		bounds[2][0] = 1 + bottomXDiff / 2.f;
//
//		float[] offset = new float[] {0, -1};
//		float[] scale = new float[] {w / 2.f, -h / 2.f};
//		for (float[] coord : bounds)
//		{
//			VectorUtil.addVec2(coord, coord, offset);
//			VectorUtil.scaleVec2(coord, coord, scale);
//		}
//
//		double threshold = 1e9;
//
//		outer:
//		for (int i = 1; i <= bounds.length; i++)
//		{
//			float[] from = bounds[i-1];
//			float[] to = bounds[i % bounds.length];
//			for (float v : from)
//				if (Float.isNaN(v) || Math.abs(v) > threshold)
//					continue outer;
//			for (float v : to)
//				if (Float.isNaN(v) || Math.abs(v) > threshold)
//					continue outer;
//			g.drawLine((int) from[0], (int) from[1], (int) to[0], (int) to[1]);
//		}
//	}

	// Visualization prior to switching from prioritizing bottom left to prioritizing top left
//	private void visualizeFrustumPlane(Graphics2D g)
//	{
//		float[][] plane = getFrustumPlane(perspective, true);
//		float[] vecUpHalf = plane[2];
//		float[] vecRightHalf = plane[3];
//		float[] center = plane[0];
//		float[] vecUp = new float[3];
//		float[] vecRight = new float[3];
//
//		// Double vectors to get full edges of the plane instead of half from center
//		VectorUtil.scaleVec3(vecUp, vecUpHalf, 2);
//		VectorUtil.scaleVec3(vecRight, vecRightHalf, 2);
//
//		float[] bottomLeft = center;
//		float[] topLeft = new float[4];
//		float[] topRight = new float[4];
//		float[] bottomRight = new float[4];
//
//		// Initialize W to 1, otherwise the results go haywire
//		topLeft[3] = 1;
//		topRight[3] = 1;
//		bottomRight[3] = 1;
//
//		// Move center point to bottom left
//		VectorUtil.subVec3(bottomLeft, bottomLeft, vecRightHalf);
//		VectorUtil.subVec3(bottomLeft, bottomLeft, vecUpHalf);
//
//		// Calculate other corners from bottom left and full edge vectors
//		VectorUtil.addVec3(topLeft, bottomLeft, vecUp);
//		VectorUtil.addVec3(topRight, topLeft, vecRight);
//		VectorUtil.subVec3(bottomRight, topRight, vecUp);
//
//		float[][] bounds = {
//			bottomLeft,
//			topLeft,
//			topRight,
//			bottomRight,
//		};
//
////		System.out.println("bounds: " + Arrays.deepToString(bounds));
//
//		for (int i = 0; i < bounds.length; i++)
//		{
//			float[] result = new float[4];
//			perspective.multVec(bounds[i], result);
//			perspectiveDivide(result);
//			bounds[i] = result;
//		}
//
//		System.out.println("bounds: " + Arrays.deepToString(bounds));
//
//		float[] offset = new float[] {1, -1};
//		float[] scale = new float[] {w / 2.f, -h / 2.f};
//		for (float[] coord : bounds)
//		{
//			VectorUtil.addVec2(coord, coord, offset);
//			VectorUtil.scaleVec2(coord, coord, scale);
//		}
//
//		double threshold = 1e9;
//
//		outer:
//		for (int i = 1; i <= bounds.length; i++)
//		{
//			float[] from = bounds[i-1];
//			float[] to = bounds[i % bounds.length];
//			for (float v : from)
//				if (Float.isNaN(v) || Math.abs(v) > threshold)
//					continue outer;
//			for (float v : to)
//				if (Float.isNaN(v) || Math.abs(v) > threshold)
//					continue outer;
//			g.drawLine((int) from[0], (int) from[1], (int) to[0], (int) to[1]);
//		}
//	}

	private void visualizeFrustumBounds(Graphics2D g)
	{
		// just visualizes the bounds to see that they line up
		float[][] bounds = getFrustumBounds(perspective, 1);
		for (int i = 0; i < bounds.length; i++)
		{
			float[] result = new float[4];
			perspective.multVec(bounds[i], result);
			perspectiveDivide(result);
			bounds[i] = result;
		}

		float[] offset = new float[] {1, 1};
		float[] scale = new float[] {w / 2.f, h / 2.f};
		for (float[] coord : bounds)
		{
			VectorUtil.addVec2(coord, coord, offset);
			VectorUtil.scaleVec2(coord, coord, scale);
		}

		double threshold = 1e7;

		outer:
		for (int i = 1; i <= bounds.length; i++)
		{
			float[] from = bounds[i-1];
			float[] to = bounds[i % bounds.length];
			for (float v : from)
				if (Float.isNaN(v) || Math.abs(v) > threshold)
					continue outer;
			for (float v : to)
				if (Float.isNaN(v) || Math.abs(v) > threshold)
					continue outer;
			g.drawLine((int) from[0], (int) from[1], (int) to[0], (int) to[1]);
		}
	}

	private float[][][] getBoundsIntersections(float[] rayOrigin, float[] rayDirection)
	{
		float[][][] intersections = new float[3][2][];

		int[][] bounds = getLocalBounds(); // TODO: needs Z bounds added back in
		for (int axis = 0; axis < bounds.length; axis++)
		{
			float[] normal = new float[3];
			float[] point = new float[3];

			for (int i = 0; i < bounds[axis].length; i++)
			{
				int other = bounds[axis][(i + 1) % 2];
				normal[axis] = other < bounds[axis][i] ? 1 : -1; // The normal should be facing inwards towards the scene

				point[axis] = bounds[axis][i];
				intersections[axis][i] = intersectPlane(rayOrigin, rayDirection, point, normal);
//				print("axis: " + axis + ", i: " + i + ": ", intersections[axis][i]);
//				return intersections;
			}
		}

		return intersections;
	}

	private float vectorLength(float[] vec3)
	{
		return (float) Math.sqrt(Math.pow(vec3[0], 2) + Math.pow(vec3[1], 2) + Math.pow(vec3[2], 2));
	}

	private float[] intersectPlane(
		float[] rayOrigin, float[] rayDirection, float[] planeOrigin, float[] planeNormal)
	{
		float[] intersection = null;

		// assume normalized direction and normal
		// t = dot(planeOrigin - rayOrigin, planeNormal) / dot(rayDirection, planeNormal)
		float denominator = VectorUtil.dotVec3(rayDirection, planeNormal);
		if (denominator > 1e-6)
//		if (denominator != 0) // includes intersections behind camera
//		if (Math.abs(denominator) > 1e-6) // includes intersections behind camera
		{
			// planeOrigin - rayOrigin
			float[] vecPR = new float[3];
			VectorUtil.subVec3(vecPR, planeOrigin, rayOrigin);
			float t = VectorUtil.dotVec3(vecPR, planeNormal) / denominator;
			if (t >= 0)
			{
				// include t in result because we use it to compare
				intersection = new float[] { 0, 0, 0, t };
				VectorUtil.scaleVec3(intersection, rayDirection, t);
				VectorUtil.addVec3(intersection, intersection, rayOrigin);
//				intersection[3] = vectorLength(intersection);
			}
		}

		return intersection;
	}

	private float[] getClosestIntersection(float[][][] intersections)
	{
		float[] nearestIntersection = null;
		for (float[][] axis : intersections)
		{
			for (float[] intersection : axis)
			{
				if (nearestIntersection == null || (intersection != null && intersection[3] < nearestIntersection[3]))
					nearestIntersection = intersection;
			}
		}
		if (nearestIntersection != null)
			nearestIntersection[3] = 1; // Make it an actual point vec4
		return nearestIntersection;
	}

	private void visualiseCornerBoundsIntersection(Graphics2D g, Matrix4 projection, Matrix4 inverseProjection, float[] screenPoint)
	{
		screenPoint[2] = 1; // set Z to far plane
		screenPoint[3] = 1; // set W to get point at infinite distance
//		print(screenPoint);

		// Project from screen to world space
		float[] worldPoint = new float[4];
		inverseProjection.multVec(screenPoint, worldPoint);
//		print(worldPoint);

		// Don't perspective divide since it's infinitely far away
		float[] dir = worldPoint;
		swapYZ(dir); // Swap Y and Z to get depth/height on Z instead of Y
		// Normalization isn't strictly necessary, but without we might skip certain intersections
		VectorUtil.normalizeVec3(dir);

		float[] camPos = new float[]
			{
				client.getCameraX(),
				client.getCameraY(),
				client.getCameraZ()
			};
//		print(camPos);

		float[][][] intersections = getBoundsIntersections(camPos, dir);
//		System.out.println(Arrays.deepToString(intersections));

//		for (float[][] axis : intersections)
//		{
//			for (float[] intersection : axis)
//			{
//				if (intersection == null)
//					continue;
//				intersection[3] = 1; // Set W to 1 as normal, since we don't want to do the calculation with T
//				swapYZ(intersection); // Swap back to RS coordinates where -Z is up
//
//				float[] screenSpacePoint = new float[4];
//				projection.multVec(intersection, screenSpacePoint);
//				perspectiveDivide(screenSpacePoint);
//
////				print("intersection screen space: ", screenSpacePoint);
//				drawPoint(g, screenSpacePoint);
//				return;
//			}
//		}

//		System.out.println(Arrays.toString(screenPoint) + ": " + Arrays.deepToString(intersections));
		float[] closest = getClosestIntersection(intersections);
		if (closest != null)
		{
//			print("xyz: ", closest);

			closest[3] = 1; // Set W to 1 as normal, since we don't want to
			// do the calculation with the intersection's distance multiplier
			swapYZ(closest); // Swap back to RS coordinates where -Z is up

//			print(closest);

			// Project from world back to screen
			float[] result = new float[4];
			projection.multVec(closest, result);
			perspectiveDivide(result);
//			print("screen xyzw: ", result);

			drawPoint(g, result);
		}
		else
		{
			System.out.println("no intersection");
		}
	}

//	private void projectPoint(Graphics2D g, Matrix4 projection, float[] screenPoint)
//	{
//		Matrix4 inverseProjection = new Matrix4();
//		inverseProjection.multMatrix(projection);
//		inverseProjection.invert();
//
//		final int viewportHeight = client.getViewportHeight();
//		final int viewportWidth = client.getViewportWidth();
////		projection.scale((float) viewportWidth / (float) w, (float) viewportHeight / (float) h, 1);
////		projection.scale((float) w / viewportWidth, (float) h / viewportHeight, 1);
//
//		screenPoint[2] = 1; // set Z
//		screenPoint[3] = 1; // set W
//
//		// Project from screen to world
//		float[] worldPoint = new float[4];
//		inverseProjection.multVec(screenPoint, worldPoint);
////		perspectiveDivide(worldPoint);
////		print(worldPoint);
//
////		worldPoint[1] = 0;
//		worldPoint[3] = 1;
//
//		// Intersection with Z = 0 from camera along dir
//		float[] dir = worldPoint;
//		float t = -client.getCameraY2() / dir[1];
//
//		if (t >= 0)
//		{
//			// Intersection with ground plane in front of the camera
//			float x = client.getCameraX2() + dir[0] * t;
//			float y = 0;
//			float z = client.getCameraZ2() + dir[2] * t;
//
//			print(new float[] {x, y, z, t});
//
//			worldPoint[0] = x;
//			worldPoint[1] = y;
//			worldPoint[2] = z;
//			worldPoint[3] = 1;
//		}
//		else
//		{
//			// Intersection behind camera, so check for intersection with vertical plane at the horizon
//
//
////			print(new float[] {x, y, z, t});
////
////			worldPoint[0] = x;
////			worldPoint[1] = y;
////			worldPoint[2] = z;
////			worldPoint[3] = 1;
//		}
//
//		// Project from world back to screen
//		float[] result = new float[4];
//		projection.multVec(worldPoint, result);
//
//		perspectiveDivide(result);
////		print(result);
//
//		drawPoint(g, result);
//	}

	private String prevPrint = "";
	private String prevPrintMultidim = "";

	private void print(float[] vec)
	{
		print("", vec);
	}

	private void print(float[][] multidim)
	{
		print("", multidim);
	}

	private void print(String prefix, float[][] multidim)
	{
		String s = prefix;
		for (float[] vec : multidim)
		{
			if (vec == null)
			{
				s += "null";
				continue;
			}
			for (int i = 0; i < vec.length; i++)
			{
				if (i > 0)
				{
					s += ", ";
				}
				s += StringUtils.leftPad(String.format("%.10f", vec[i]), 10);
			}
			s += "      ";
		}
		if (!prevPrintMultidim.equals(s))
		{
			System.out.println(s);
			prevPrintMultidim = s;
		}
	}

	private void print(String prefix, float[] vec)
	{
		if (vec == null)
		{
			System.out.println(prefix + "null");
			return;
		}
		String s = prefix;
		for (int i = 0; i < vec.length; i++)
		{
			if (i > 0)
			{
				s += ", ";
			}
			s += StringUtils.leftPad(String.format("%.10f", vec[i]), 10);
		}
		if (!prevPrint.equals(s))
		{
			System.out.println(s);
			prevPrint = s;
		}
	}

	private float[] worldIntersectionFromClipSpace(Matrix4 inverseProjection, float clipX, float clipY)
	{
		float groundZ = 0;

		float[] clip = new float[]{clipX, clipY, groundZ, 1};

		float[] dir = new float[4];
		inverseProjection.multVec(clip, dir);

//		System.out.println(Arrays.toString(dir));
//		perspectiveDivide(dir);
		return dir;
//		System.out.println(Arrays.toString(dir));

//		// Intersection with Z = 0 from camera along dir
//		float t = -client.getCameraY2() / dir[1];
//		float x = client.getCameraX2() + dir[0] * t;
//		float y = 0;
//		float z = client.getCameraZ2() + dir[2] * t;
//
//		return new float[] {x, y, z};
	}

	private float[][] getViewportBoundsWorldSpace(Matrix4 projection)
	{
		Matrix4 inverseProjection = new Matrix4();
		inverseProjection.multMatrix(projection);
		inverseProjection.invert();

		float[] clipTL = worldIntersectionFromClipSpace(inverseProjection, -1, 1);
		float[] clipTR = worldIntersectionFromClipSpace(inverseProjection, 1, 1);
		float[] clipBL = worldIntersectionFromClipSpace(inverseProjection, -1, -1);
		float[] clipBR = worldIntersectionFromClipSpace(inverseProjection, 1, -1);

		return new float[][]{clipTL, clipTR, clipBL, clipBR};
	}

	private void perspectiveDivide(float[] vec4)
	{
		vec4[0] /= vec4[3];
		vec4[1] /= vec4[3];
		vec4[2] /= vec4[3];
		vec4[3] = 1;
	}

	private float[] makePerspectiveProjectionMatrix(float w, float h, float n)
	{
		return new float[]
			{
				2 / w, 0, 0, 0,
				0, 2 / h, 0, 0,
				0, 0, -1, -1,
				0, 0, -2 * n, 0
			};
	}

	private void swapYZ(float[] vec)
	{
		swapAxes(vec, 1, 2);
	}

	private void swapAxes(float[] vec, int axisA, int axisB)
	{
		float tmp = vec[axisA];
		vec[axisA] = vec[axisB];
		vec[axisB] = tmp;
	}

	//	private float[] getLocalBounds()
	//	{
	//		float[] bounds = new float[4];
	//
	//		Player p = client.getLocalPlayer();
	//		if (p != null)
	//		{
	//			LocalPoint l = p.getLocalLocation();
	//
	//			int localX = l.getX();
	//			int localY = l.getY();
	//
	//			float sceneX = (float) l.getX() / Perspective.LOCAL_TILE_SIZE;
	//			float sceneY = (float) l.getY() / Perspective.LOCAL_TILE_SIZE;
	//
	//			float minLocalX = (1 - sceneX) * Perspective.LOCAL_TILE_SIZE + localX;
	//			float minLocalY = (1 - sceneY) * Perspective.LOCAL_TILE_SIZE + localY;
	//			float maxLocalX = (Perspective.SCENE_SIZE - 2 - sceneX) * Perspective.LOCAL_TILE_SIZE + localX;
	//			float maxLocalY = (Perspective.SCENE_SIZE - 2 - sceneY) * Perspective.LOCAL_TILE_SIZE + localY;
	//
	//			bounds[0] = minLocalX;
	//			bounds[1] = minLocalY;
	//			bounds[2] = maxLocalX;
	//			bounds[3] = maxLocalY;
	//		}
	//
	//		return bounds;
	//	}

	public Matrix4 getPerspectiveProjection()
	{
		final int viewportHeight = client.getViewportHeight();
		final int viewportWidth = client.getViewportWidth();
		final int yaw = client.getCameraYaw();
		final int pitch = client.getCameraPitch();

		Matrix4 projection = new Matrix4();
		projection.scale(client.getScale(), client.getScale(), 1);
		projection.multMatrix(makePerspectiveProjectionMatrix(viewportWidth, viewportHeight, 50));
		projection.rotate((float) (Math.PI - pitch * Perspective.UNIT), -1, 0, 0);
		projection.rotate((float) (yaw * Perspective.UNIT), 0, 1, 0);
		projection.translate(-client.getCameraX2(), -client.getCameraY2(), -client.getCameraZ2());

		return projection;
	}

	private void centerMatrix(Matrix4 matrix, float[] origin)
	{
		matrix.translate(-origin[0], -origin[1], -origin[2]);
	}

	private void constrainPointToBounds(float[] point, int[][] bounds)
	{
		for (int axis = 0; axis < 2; axis++)
		{
			if (point[axis] < bounds[axis][0])
			{
				System.out.println("bounding " + point[axis] + " to " + bounds[axis][0]);
				point[axis] = bounds[axis][0];
			}
			else if (point[axis] > bounds[axis][1])
			{
				System.out.println("bounding " + point[axis] + " to " + bounds[axis][1]);
				point[axis] = bounds[axis][1];
			}
		}
	}
}
