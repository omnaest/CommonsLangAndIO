/*

	Copyright 2017 Danny Kunz

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

		http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.


*/
package org.omnaest.utils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.omnaest.utils.FileUtils.BatchFileReader.BatchFileReaderLoaded;

public class FileUtils
{
	/**
	 * {@link Consumer} of {@link String} which will be written into the underlying {@link File}
	 * 
	 * @author omnaest
	 */
	public static interface FileStringContentConsumer extends Consumer<String>
	{
		public FileStringContentConsumer using(Charset charset);

		public FileStringContentConsumer usingUTF8();

		/**
		 * Writes the given {@link String} to the underlying {@link File}.<br>
		 * <br>
		 * If null is given, then the file is deleted
		 * 
		 * @throws FileAccessException
		 *             for any {@link IOException}
		 */
		@Override
		void accept(String data);
	}

	/**
	 * {@link Supplier} which returns the content of a {@link File} as {@link String}
	 * 
	 * @author omnaest
	 */
	public static interface FileStringContentSupplier extends Supplier<String>
	{
		public FileStringContentSupplier using(Charset charset);

		public FileStringContentSupplier usingUTF8();

		/**
		 * Returns the content of the underlying {@link File} as {@link String}.<br>
		 * <br>
		 * Returns null if the {@link File} does not exists, but throws an {@link FileAccessException} in other {@link IOException} cases.
		 * 
		 * @throws FileAccessException
		 *             for any {@link IOException}
		 */
		@Override
		public String get();
	}

	/**
	 * @author omnaest
	 */
	public static class FileAccessException extends IllegalStateException
	{
		private static final long serialVersionUID = -6516326101828174660L;

		public FileAccessException(Throwable cause)
		{
			super(cause);
		}
	}

	/**
	 * Returns a {@link FileStringContentConsumer} for the given {@link File}
	 * 
	 * @param file
	 * @return
	 */
	public static FileStringContentConsumer toConsumer(File file)
	{
		return new FileStringContentConsumer()
		{
			private Charset charset = StandardCharsets.UTF_8;

			@Override
			public FileStringContentConsumer using(Charset charset)
			{
				this.charset = charset;
				return this;
			}

			@Override
			public FileStringContentConsumer usingUTF8()
			{
				return this.using(StandardCharsets.UTF_8);
			}

			@Override
			public void accept(String data)
			{
				try
				{
					if (data != null)
					{
						org.apache.commons.io.FileUtils.write(file, data, this.charset);
					}
					else
					{
						org.apache.commons.io.FileUtils.deleteQuietly(file);
					}
				} catch (IOException e)
				{
					throw new FileAccessException(e);
				}

			}
		};
	}

	/**
	 * Returns a {@link FileStringContentSupplier} for the given {@link File}
	 * 
	 * @param file
	 * @return
	 */
	public static FileStringContentSupplier toSupplier(File file)
	{
		return new FileStringContentSupplier()
		{
			private Charset charset = StandardCharsets.UTF_8;

			@Override
			public FileStringContentSupplier usingUTF8()
			{
				return this.using(StandardCharsets.UTF_8);
			}

			@Override
			public FileStringContentSupplier using(Charset charset)
			{
				this.charset = charset;
				return this;
			}

			@Override
			public String get()
			{
				String retval = null;
				try
				{
					if (file.exists() && file.isFile())
					{
						retval = org.apache.commons.io.FileUtils.readFileToString(file, this.charset);
					}
				} catch (IOException e)
				{
					throw new FileAccessException(e);
				}
				return retval;
			}
		};
	}

	public static File createRandomTempFile() throws IOException
	{
		File tempFile = Files	.createTempFile("", "")
								.toFile();
		tempFile.deleteOnExit();
		return tempFile;
	}

	public static interface BatchFileReader
	{
		public BatchFileReaderLoaded from(File... files);

		public BatchFileReaderLoaded fromDirectory(File directory);

		public BatchFileReaderLoaded from(File file);

		public static interface BatchFileReaderLoaded extends BatchFileReader
		{
			public BatchFileReaderLoaded usingEncoding(Charset encoding);

			public Stream<String> getAsStringStream();

			public Stream<String> getAsLinesStream();
		}

	}

	public static class FileAccessRuntimeException extends RuntimeException
	{
		private static final long serialVersionUID = -4087596140531337664L;

		public FileAccessRuntimeException(Throwable cause)
		{
			super(cause);
		}

	}

	/**
	 * Returns a new {@link BatchFileReader} instance.<br>
	 * <br>
	 * Example:<br>
	 * 
	 * <pre>
	 * List<String> lines = FileUtils.read()
	 * 								.from(tempFile1, tempFile2)
	 * 								.getAsLinesStream()
	 * 								.collect(Collectors.toList());
	 * </pre>
	 * 
	 * @return
	 */
	public static BatchFileReader read()
	{
		return new BatchFileReaderLoaded()
		{
			private List<File>	files		= new ArrayList<>();
			private Charset		encoding	= StandardCharsets.UTF_8;

			@Override
			public BatchFileReaderLoaded from(File... files)
			{
				if (files != null)
				{
					for (File file : files)
					{
						this.from(file);
					}
				}
				return this;
			}

			@Override
			public BatchFileReaderLoaded from(File file)
			{
				if (file != null)
				{
					if (file.isFile())
					{
						this.files.add(file);
					}
					else if (file.isDirectory())
					{
						this.fromDirectory(file);
					}
				}
				return this;
			}

			@Override
			public BatchFileReaderLoaded fromDirectory(File directory)
			{
				if (directory != null)
				{
					this.files.addAll(Arrays.asList(directory.listFiles((FileFilter) file -> file.isFile())));
				}
				return this;
			}

			@Override
			public BatchFileReaderLoaded usingEncoding(Charset encoding)
			{
				this.encoding = encoding;
				return this;
			}

			@Override
			public Stream<String> getAsStringStream()
			{
				return this.files	.stream()
									.filter(PredicateUtils.notNull())
									.map(file ->
									{
										try
										{
											return org.apache.commons.io.FileUtils.readFileToString(file, this.encoding);
										} catch (IOException e)
										{
											throw new FileAccessRuntimeException(e);
										}
									});
			}

			@Override
			public Stream<String> getAsLinesStream()
			{
				return this.files	.stream()
									.filter(PredicateUtils.notNull())
									.flatMap(file ->
									{
										try
										{
											return org.apache.commons.io.FileUtils	.readLines(file, this.encoding)
																					.stream();
										} catch (IOException e)
										{
											throw new FileAccessRuntimeException(e);
										}
									});
			}
		};
	}
}
