package hu.skzs.multiproperties.handler.text.writer;

import hu.skzs.multiproperties.base.api.HandlerException;

/**
 * An {@link IWriter} implementations are responsible for writing the output file.
 * @author sallai
 */
public interface IWriter
{

	/**
	 * Writes the given <code>bytes</code> content.
	 * @param bytes the given content in byte array
	 * @throws HandlerException when writing failed
	 */
	public abstract void write(byte[] bytes) throws HandlerException;
}