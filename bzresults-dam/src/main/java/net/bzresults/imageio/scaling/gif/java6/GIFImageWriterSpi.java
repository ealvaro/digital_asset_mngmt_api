/**
 * 
 */
package net.bzresults.imageio.scaling.gif.java6;

import java.awt.image.ColorModel;
import java.awt.image.SampleModel;
import java.util.Locale;

import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.spi.ImageWriterSpi;

public class GIFImageWriterSpi extends ImageWriterSpi {

    private static final String vendorName = "Sun Microsystems, Inc.";

    private static final String version = "1.0";

    private static final String[] names = { "gif", "GIF" };

    private static final String[] suffixes = { "gif" };

    private static final String[] MIMETypes = { "image/gif" };

    private static final String writerClassName =
    "com.sun.imageio.plugins.gif.GIFImageWriter";

    private static final String[] readerSpiNames = {
        "com.sun.imageio.plugins.gif.GIFImageReaderSpi"
    };

    public GIFImageWriterSpi() {
        super(vendorName,
              version,
              names,
              suffixes,
              MIMETypes,
              writerClassName,
              STANDARD_OUTPUT_TYPE,
              readerSpiNames,
              true,
              GIFWritableStreamMetadata.NATIVE_FORMAT_NAME,
              "com.sun.imageio.plugins.gif.GIFStreamMetadataFormat",
              null, null,
              true,
              GIFWritableImageMetadata.NATIVE_FORMAT_NAME,
              "com.sun.imageio.plugins.gif.GIFImageMetadataFormat",
              null, null
              );
    }

    public boolean canEncodeImage(ImageTypeSpecifier type) {
        if (type == null) {
            throw new IllegalArgumentException("type == null!");
        }

        SampleModel sm = type.getSampleModel();
        ColorModel cm = type.getColorModel();

        boolean canEncode = sm.getNumBands() == 1 &&
            sm.getSampleSize(0) <= 8 &&
            sm.getWidth() <= 65535 &&
            sm.getHeight() <= 65535 &&
            (cm == null || cm.getComponentSize()[0] <= 8);

        if (canEncode) {
            return true;
        } else {
            return PaletteBuilder.canCreatePalette(type);
        }
    }

    public String getDescription(Locale locale) {
        return "Standard GIF image writer";
    }

    public ImageWriter createWriterInstance(Object extension) {
        return new GIFImageWriter(this);
    }
}
