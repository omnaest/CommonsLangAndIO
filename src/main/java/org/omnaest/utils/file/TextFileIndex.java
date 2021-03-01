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
package org.omnaest.utils.file;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.omnaest.utils.element.bi.BiElement;

/**
 * An index of text {@link File}s which are addressed by a {@link String} key.
 * 
 * @author omnaest
 */
public interface TextFileIndex extends AutoCloseable
{
    public TextFileIndex put(String key, String value);

    public Optional<String> get(String key);

    public TextFileIndex clear();

    public TextFileIndex remove(String key);

    public Stream<String> keys();

    public default TextFileIndex putAll(Map<String, String> map)
    {
        Optional.ofNullable(map)
                .orElse(Collections.emptyMap())
                .forEach((key, value) -> this.put(key, value));
        return this;
    }

    public default Map<String, String> getAll(Collection<String> keys)
    {
        return Optional.ofNullable(keys)
                       .orElse(Collections.emptyList())
                       .stream()
                       .distinct()
                       .map(key -> BiElement.of(key, this.get(key)))
                       .filter(keyAndValue -> keyAndValue.getSecond()
                                                         .isPresent())
                       .map(keyAndValue -> keyAndValue.applyToSecondArgument(Optional::get))
                       .collect(Collectors.toMap(BiElement::getFirst, BiElement::getSecond));
    }
}
