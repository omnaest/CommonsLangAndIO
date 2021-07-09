package org.omnaest.utils;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;

/**
 * @author omnaest
 */
public class ArgumentsUtils
{
    public static Arguments parse(String... args)
    {
        return new Arguments()
        {
            private ArgumentsConditionImpl argumentsCondition = new ArgumentsConditionImpl(this);

            @Override
            public Optional<Parameter> getParameter(String name)
            {
                return IntStream.range(0, args.length)
                                .boxed()
                                .filter(index ->
                                {
                                    String token = args[index];
                                    return StringUtils.startsWith(token, "-") && StringUtils.equalsIgnoreCase(name, StringUtils.removeStart(token, "-"));
                                })
                                .findFirst()
                                .map(index -> new Parameter()
                                {
                                    @Override
                                    public Optional<Argument> getFirstArgument()
                                    {
                                        return this.getFollowingArgument(0);
                                    }

                                    @Override
                                    public Optional<Argument> getFollowingArgument(int subIndex)
                                    {
                                        return getArgument(index + 1 + subIndex);
                                    }
                                });
            }

            @Override
            public Optional<Argument> getArgument(int index)
            {
                return Optional.ofNullable(args)
                               .filter(a -> index >= 0 && index < args.length)
                               .map(arg -> new Argument()
                               {
                                   @Override
                                   public String get()
                                   {
                                       return args[index];
                                   }

                                   @Override
                                   public <R> R mapTo(Function<String, R> mapper)
                                   {
                                       return mapper.apply(this.get());
                                   }

                                   @Override
                                   public int getAsInt()
                                   {
                                       return NumberUtils.toInt(this.get());
                                   }
                               });
            }

            @Override
            public ArgumentsCondition ifAllParametersArePresent(String... allParameterTokens)
            {
                return this.argumentsCondition.ifAllParametersArePresent(allParameterTokens);
            }
        };
    }

    private static class ArgumentsConditionImpl implements ArgumentsConditionInitializer, ArgumentsCondition
    {
        private final Arguments arguments;
        private final boolean   currentChainSuccess;
        private final boolean   anyChainSuccess;

        public ArgumentsConditionImpl(Arguments arguments, boolean currentChainSuccess, boolean anyChainSuccess)
        {
            super();
            this.arguments = arguments;
            this.currentChainSuccess = currentChainSuccess;
            this.anyChainSuccess = anyChainSuccess;
        }

        public ArgumentsConditionImpl(Arguments arguments)
        {
            this(arguments, false, false);
        }

        @Override
        public ArgumentsCondition ifAllParametersArePresent(String... allParameterTokens)
        {
            boolean nextChainSuccess = Arrays.asList(allParameterTokens)
                                             .stream()
                                             .allMatch(token -> this.arguments.getParameter(token)
                                                                              .isPresent());
            return new ArgumentsConditionImpl(this.arguments, nextChainSuccess, this.determineNextAnyChainSuccess());
        }

        private boolean determineNextAnyChainSuccess()
        {
            return this.currentChainSuccess || this.anyChainSuccess;
        }

        @Override
        public ArgumentsCondition then(Consumer<Arguments> argumentsConsumer)
        {
            if (this.currentChainSuccess)
            {
                argumentsConsumer.accept(this.arguments);
            }
            return this;
        }

        @Override
        public ArgumentsConditionInitializer orElse()
        {
            return new ArgumentsConditionImpl(this.arguments, false, this.determineNextAnyChainSuccess());
        }

        @Override
        public ArgumentsConditionInitializer orElse(Consumer<Arguments> argumentsConsumer)
        {
            if (!this.currentChainSuccess && !this.anyChainSuccess)
            {
                argumentsConsumer.accept(this.arguments);
            }
            return this.orElse();
        }

        @Override
        public ArgumentsConditionInitializer orElse(Runnable operation)
        {
            return this.orElse(arguments -> operation.run());
        }

    }

    public static interface Arguments extends ArgumentsConditionInitializer
    {
        /**
         * Gets a {@link Parameter} in the arguments which is indicated by a leading "-" character.
         * 
         * @param name
         * @return
         */
        public Optional<Parameter> getParameter(String name);

        /**
         * Returns a wrapper around the argument at the given index
         * 
         * @param index
         * @return
         */
        public Optional<Argument> getArgument(int index);

    }

    public static interface ArgumentsConditionInitializer
    {
        public ArgumentsCondition ifAllParametersArePresent(String... allParameterTokens);
    }

    public static interface ArgumentsCondition
    {
        public ArgumentsCondition then(Consumer<Arguments> argumentsConsumer);

        public ArgumentsConditionInitializer orElse();

        public ArgumentsConditionInitializer orElse(Consumer<Arguments> argumentsConsumer);

        public ArgumentsConditionInitializer orElse(Runnable operation);
    }

    public static interface Argument extends Supplier<String>
    {
        public <R> R mapTo(Function<String, R> mapper);

        public int getAsInt();
    }

    public static interface Parameter
    {
        /**
         * Returns the first {@link Argument} directly after the parameter key
         * 
         * @return
         */
        public Optional<Argument> getFirstArgument();

        /**
         * Returns the n-th {@link Argument} after the parameter key.
         * 
         * @param subIndex
         *            = 0, 1, 2, ... where 0 = the first argument after the parameter
         * @return
         */
        public Optional<Argument> getFollowingArgument(int subIndex);
    }
}
