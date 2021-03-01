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
package org.omnaest.utils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.ArrayUtils;
import org.omnaest.utils.functional.Builder;

public class EncoderUtils
{
    public static EncoderAndDecoderFactory newInstance()
    {
        return new EncoderAndDecoderFactory()
        {
            @Override
            public TextEncoderAndDecoder forAlphaNumericText()
            {
                return this.forText()
                           .withAllowedCharactersRegEx("[a-zA-Z0-9]")
                           .build();
            }

            @Override
            public IntermediateTextEncoderAndDecoder<IntStream> forUTF8TextAsIntStream()
            {
                return new UTF8AsIntStreamEncoderDecoder();
            }

            @Override
            public TextEncoderAndDecoderBuilder forText()
            {
                return new TextEncoderAndDecoderBuilder()
                {
                    private Pattern allowedCharactersPattern = Pattern.compile(".");

                    @Override
                    public TextEncoderAndDecoderBuilder withAllowedCharacters(String... characters)
                    {
                        return this.withAllowedCharacters(Arrays.asList(characters));
                    }

                    @Override
                    public TextEncoderAndDecoderBuilder withAllowedCharacters(List<String> characters)
                    {
                        this.allowedCharactersPattern = Pattern.compile("[" + characters.stream()
                                                                                        .map(character -> Pattern.quote(character))
                                                                                        .collect(Collectors.joining(""))
                                + "]");
                        return this;
                    }

                    @Override
                    public TextEncoderAndDecoderBuilder withAllowedCharactersRegEx(Pattern pattern)
                    {
                        this.allowedCharactersPattern = pattern;
                        return this;
                    }

                    @Override
                    public TextEncoderAndDecoderBuilder withAllowedCharactersRegEx(String regex)
                    {
                        return this.withAllowedCharactersRegEx(Pattern.compile(regex));
                    }

                    @Override
                    public TextEncoderAndDecoder build()
                    {
                        return new GenericTextEncoderAndDecoder(this.allowedCharactersPattern);
                    }
                };
            }
        };
    }

    public static interface TextEncoderAndDecoderBuilder extends Builder<TextEncoderAndDecoder>
    {
        public TextEncoderAndDecoderBuilder withAllowedCharacters(List<String> characters);

        public TextEncoderAndDecoderBuilder withAllowedCharacters(String... characters);

        public TextEncoderAndDecoderBuilder withAllowedCharactersRegEx(String regex);

        public TextEncoderAndDecoderBuilder withAllowedCharactersRegEx(Pattern pattern);
    }

    public static interface TextEncoderAndDecoderFactory
    {
        public TextEncoderAndDecoder forAlphaNumericText();

        public TextEncoderAndDecoderBuilder forText();
    }

    public static interface EncoderAndDecoderFactory extends TextEncoderAndDecoderFactory
    {
        public IntermediateTextEncoderAndDecoder<IntStream> forUTF8TextAsIntStream();
    }

    public static interface EncoderAndDecoder<E, T, D> extends Encoder<E, T>, Decoder<T, D>
    {
    }

    public static interface UnaryEncoderAndDecoder<T> extends EncoderAndDecoder<T, T, T>
    {
    }

    public static interface TextEncoderAndDecoder extends UnaryEncoderAndDecoder<String>
    {
    }

    public static interface IntermediateTextEncoderAndDecoder<T> extends EncoderAndDecoder<String, T, String>
    {
    }

    public static interface Encoder<FROM, TO>
    {
        public TO encode(FROM object);
    }

    public static interface Decoder<FROM, TO>
    {
        public TO decode(FROM object);
    }

    protected static class GenericTextEncoderAndDecoder implements TextEncoderAndDecoder
    {
        private static final String DELIMITER = "_";

        private Pattern                                      allowedCharactersPattern;
        private IntermediateTextEncoderAndDecoder<IntStream> utf8EncoderDecoder = newInstance().forUTF8TextAsIntStream();

        public GenericTextEncoderAndDecoder(Pattern allowedCharactersPattern)
        {
            super();
            this.allowedCharactersPattern = allowedCharactersPattern;
        }

        @Override
        public String encode(String text)
        {
            return StringUtils.splitToStream(text)
                              .map(character ->
                              {
                                  if (this.allowedCharactersPattern.matcher(character)
                                                                   .matches()
                                          && !org.apache.commons.lang3.StringUtils.equals(DELIMITER, character))
                                  {
                                      return character;
                                  }
                                  else
                                  {
                                      return DELIMITER + this.utf8EncoderDecoder.encode(character)
                                                                                .mapToObj(v -> "" + v)
                                                                                .collect(Collectors.joining(DELIMITER))
                                              + DELIMITER;
                                  }
                              })
                              .collect(Collectors.joining());
        }

        @Override
        public String decode(String text)
        {
            return MatcherUtils.matcher()
                               .of(Pattern.compile("\\" + DELIMITER + "[\\-0-9\\" + DELIMITER + "]*\\" + DELIMITER))
                               .findInAnd(text)
                               .replace(encodedStringBlock -> this.utf8EncoderDecoder.decode(StringUtils.splitToStream(StringUtils.removeStartAndEnd(encodedStringBlock,
                                                                                                                                                     1, 1),
                                                                                                                       DELIMITER)
                                                                                                        .filter(PredicateUtils.notEmpty())
                                                                                                        .map(token -> Integer.valueOf(token))
                                                                                                        .mapToInt(v -> v)));

        }

    }

    protected static class UTF8AsIntStreamEncoderDecoder extends TextAsIntStreamEncoderDecoder
    {
        public UTF8AsIntStreamEncoderDecoder()
        {
            super(StandardCharsets.UTF_8);
        }
    }

    protected static class TextAsIntStreamEncoderDecoder implements IntermediateTextEncoderAndDecoder<IntStream>
    {
        private Charset charset;

        public TextAsIntStreamEncoderDecoder(Charset charset)
        {
            super();
            this.charset = charset;
        }

        @Override
        public IntStream encode(String text)
        {
            return Arrays.asList(ArrayUtils.toObject(text.getBytes(this.charset)))
                         .stream()
                         .mapToInt(v -> v);
        }

        @Override
        public String decode(IntStream intStream)
        {
            Byte[] codes = intStream.mapToObj(v -> Byte.valueOf((byte) v))
                                    .toArray(v -> new Byte[v]);
            byte[] data = ArrayUtils.toPrimitive(codes);
            return new String(data, this.charset);
        }

    }

}
