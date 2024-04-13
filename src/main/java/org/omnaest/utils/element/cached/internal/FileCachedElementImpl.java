/*******************************************************************************
 * Copyright 2021 Danny Kunz
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
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
package org.omnaest.utils.element.cached.internal;

import java.io.File;
import java.io.Reader;
import java.io.Writer;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.omnaest.utils.FileUtils;
import org.omnaest.utils.element.cached.CachedElement;

/**
 * @see CachedElement#of(Supplier)
 * @author omnaest
 * @param <E>
 */
public class FileCachedElementImpl<E> implements CachedElement<E>
{
    private AtomicReference<Supplier<E>> supplier = new AtomicReference<>();
    private File                         file;
    private Consumer<E>                  toFileConsumer;
    private Supplier<E>                  fromFileSupplier;

    public FileCachedElementImpl(Supplier<E> supplier, File file, Function<E, String> serializer, Function<String, E> deserializer)
    {
        super();
        this.file = file;
        this.toFileConsumer = FileUtils.toConsumer(file)
                                       .with(serializer);

        this.fromFileSupplier = FileUtils.toSupplier(file)
                                         .with(deserializer);
        this.supplier.set(supplier);
    }

    public FileCachedElementImpl(Supplier<E> supplier, File file, BiConsumer<E, Writer> serializer, Function<Reader, E> deserializer)
    {
        super();
        this.file = file;
        this.toFileConsumer = FileUtils.toWriterSupplierUTF8(file)
                                       .toConsumerWith(serializer);

        this.fromFileSupplier = FileUtils.toReaderSupplierUTF8(file)
                                         .toSupplier(deserializer);
        this.supplier.set(supplier);
    }

    public FileCachedElementImpl(Supplier<E> supplier, File file, InputOutputStreamSerializerAndDeserializer<E> serializerAndDeserializer)
    {
        super();
        this.file = file;
        this.toFileConsumer = FileUtils.toOutputSupplier(file)
                                       .toConsumerWith(serializerAndDeserializer);

        this.fromFileSupplier = FileUtils.toInputSupplier(file)
                                         .toSupplier(serializerAndDeserializer);
        this.supplier.set(supplier);
    }

    @Override
    public E get()
    {
        E retval = this.fromFileSupplier.get();
        retval = this.getFromSupplierIfNull(retval);
        return retval;
    }

    @Override
    public Optional<E> getIfCached()
    {
        E retval = this.fromFileSupplier.get();
        return Optional.ofNullable(retval);
    }

    private E getFromSupplierIfNull(E retval)
    {
        if (retval == null)
        {
            retval = this.supplier.get()
                                  .get();
            this.toFileConsumer.accept(retval);
        }
        return retval;
    }

    @Override
    public E getAndReset()
    {
        E retval = this.fromFileSupplier.get();
        retval = this.getFromSupplierIfNull(retval);
        this.toFileConsumer.accept(null);
        return retval;
    }

    @Override
    public CachedElement<E> reset()
    {
        this.toFileConsumer.accept(null);
        return this;
    }

    @Override
    public CachedElement<E> setSupplier(Supplier<E> supplier)
    {
        this.supplier.set(supplier);
        return this;
    }

    @Override
    public String toString()
    {
        return "FileCachedElementImpl [supplier=" + this.supplier + ", file=" + this.file + "]";
    }

    @Override
    public Supplier<E> asNonCachedSupplier()
    {
        return this.supplier.get();
    }

    @Override
    public CachedElement<E> updateValue(UnaryOperator<E> updateFunction)
    {
        this.toFileConsumer.accept(updateFunction.apply(this.fromFileSupplier.get()));
        return this;
    }

}
