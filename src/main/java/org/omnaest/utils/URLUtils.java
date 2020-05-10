package org.omnaest.utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;

public class URLUtils
{
    public static interface URLResource extends Supplier<String>
    {
        public Optional<URLResource> navigateTo(String path);

        public URLResource removeTrailingAnker();

        public boolean isValid();

        public Optional<URL> asUrl();

    }

    public static URLResource from(String url)
    {
        return new URLResourceImpl(url);
    }

    private static final class URLResourceImpl implements URLResource
    {
        private final String url;

        private URLResourceImpl(String url)
        {
            this.url = Optional.ofNullable(this.determineURL(url))
                               .map(URL::toString)
                               .orElse(null);
        }

        @Override
        public Optional<URL> asUrl()
        {
            URL urlInstance = this.determineURL(this.url);
            return Optional.ofNullable(urlInstance);
        }

        private URL determineURL(String url)
        {
            try
            {
                return new URL(url);
            }
            catch (MalformedURLException e)
            {
                return null;
            }
        }

        @Override
        public Optional<URLResource> navigateTo(String path)
        {
            return this.asUrl()
                       .map(url -> this.determineURL(url, path))
                       .map(URL::toString)
                       .map(URLResourceImpl::new);
        }

        private URL determineURL(URL url, String path)
        {
            try
            {
                return new URL(url, path);
            }
            catch (MalformedURLException e)
            {
                return null;
            }
        }

        @Override
        public boolean isValid()
        {
            return this.asUrl()
                       .isPresent();
        }

        @Override
        public String get()
        {
            return this.url;
        }

        @Override
        public URLResource removeTrailingAnker()
        {
            String newUrl = StringUtils.removePattern(this.url, "\\#[^/\\#]+$");
            return new URLResourceImpl(newUrl);
        }
    }

}
