package net.runelite.client.plugins.gpu.util;

public class ColorAttachmentList
{
	public int[] attachments;
	public int length = 0;

	/**
	 * List to dynamically map OpenGL attachments to indices.
	 * @param maxSize
	 */
	public ColorAttachmentList(int maxSize)
	{
		attachments = new int[maxSize];
	}

	/**
	 * Adds an attachment to the list and returns the new attachment's index in the list.
	 * @param glAttachmentId
	 * @return New index in the list.
	 */
	public int add(int glAttachmentId) throws IndexOutOfBoundsException
	{
		attachments[length] = glAttachmentId;
		return length++;
	}

	public int indexOf(int glAttachmentId)
	{
		for (int i = 0; i < length; i++)
		{
			if (attachments[i] == glAttachmentId)
			{
				return i;
			}
		}
		return -1;
	}
}
