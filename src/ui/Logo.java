package ui;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Finds the school logo on disk and draws it (round, smooth, at any size). */
public class Logo {
    //
    // HOW TO ADD YOUR LOGO (the easy way):
    //   Put a picture whose name contains "logo" (logo.png, logo.jpg, School-Logo.PNG ...)
    //   in the "resources" folder next to "src", or simply in the project folder.
    //   The program finds it by itself. When it starts, it prints ONE line in the terminal
    //   saying which file it used, or where it looked if it found nothing.
    //
    //   Other ways (optional):
    //     java -Dlogo=/full/path/to/picture.png -cp ... ui.Main
    //     or set the environment variable LIBRARY_LOGO to the full path.
    //
    //   Pictures Java can read: PNG, JPG, GIF, BMP. (WEBP, HEIC and SVG do not work: save as PNG.)

    private static final String[] LOGO_FOLDERS = {
            "resources", "resource", "images", "image", "img", "assets", "res", "src/resources", "src", "."};
    private static final String[] IMAGE_TYPES = {".png", ".jpg", ".jpeg", ".jfif", ".gif", ".bmp"};   // Java can open these
    private static final String[] OTHER_PICTURE_TYPES = {".webp", ".heic", ".heif", ".avif", ".svg", ".tif", ".tiff", ".ico"}; // it can't

    private static BufferedImage logoImage;
    private static boolean logoTried = false;
    private static String logoStatus = "[Logo] not loaded yet.";
    private static Image windowIcon;

    /** One sentence saying which logo file is used, or why none was found. */
    public static synchronized String logoStatus() {
        loadLogo();
        return logoStatus;
    }

    public static boolean hasLogo() {
        return loadLogo() != null;
    }

    private static synchronized BufferedImage loadLogo() {
        if (logoTried) return logoImage;
        logoTried = true;
        List<String> problems = new ArrayList<>();

        // 1. A path given on purpose: -Dlogo=... or the LIBRARY_LOGO environment variable
        String chosen = System.getProperty("logo", System.getenv("LIBRARY_LOGO"));
        if (chosen != null && !chosen.trim().isEmpty()) {
            File file = new File(chosen.trim());
            BufferedImage img = readImage(file, problems);
            if (img != null) return foundLogo(img, "Loaded " + file.getAbsolutePath());
            if (!file.exists()) problems.add("The path you gave does not exist: " + file.getAbsolutePath());
        }

        // 2. Inside the classpath (for example when the logo is packed in a .jar)
        for (String name : new String[]{"logo.png", "logo.jpg", "logo.jpeg"}) {
            try (InputStream in = Logo.class.getResourceAsStream("/" + name)) {
                if (in != null) {
                    BufferedImage img = ImageIO.read(in);
                    if (img != null) return foundLogo(img, "Loaded from the classpath: /" + name);
                }
            } catch (IOException ignored) {
                // try the next place
            }
        }

        // 3. Search the usual folders for any picture with "logo" in its name
        List<File> searched = new ArrayList<>();
        List<String> seen = new ArrayList<>();
        List<File> candidates = new ArrayList<>();
        for (File base : baseFolders()) {
            for (String folder : LOGO_FOLDERS) {
                File dir = new File(base, folder);
                String key;
                try {
                    key = dir.getCanonicalPath();
                } catch (IOException e) {
                    key = dir.getAbsolutePath();
                }
                if (!dir.isDirectory() || seen.contains(key)) continue;
                seen.add(key);
                searched.add(new File(key));

                File[] files = dir.listFiles();
                if (files == null) continue;
                for (File f : files) {
                    if (f.isFile() && isPictureName(f.getName()) && f.getName().toLowerCase(Locale.ROOT).contains("logo")) {
                        candidates.add(f);
                    }
                }
            }
        }
        // plain "logo.png" / "logo.jpg" first, then any other name that contains "logo"
        candidates.sort((a, b) -> Integer.compare(logoRank(a), logoRank(b)));
        for (File file : candidates) {
            BufferedImage img = readImage(file, problems);
            if (img != null) return foundLogo(img, "Loaded " + file.getAbsolutePath());
        }

        // Nothing worked: say exactly what happened
        StringBuilder msg = new StringBuilder("[Logo] NOT FOUND - showing a plain placeholder.");
        for (String problem : problems) msg.append("\n[Logo]   Problem: ").append(problem);
        msg.append("\n[Logo]   Fix: put a picture named logo.png (or logo.jpg) in the folder: ")
                .append(new File("resources").getAbsolutePath());
        msg.append("\n[Logo]   The program was started from: ").append(new File(".").getAbsoluteFile().getParent());
        msg.append("\n[Logo]   Folders searched: ");
        if (searched.isEmpty()) msg.append("(none of the usual folders exist)");
        for (File dir : searched) msg.append("\n[Logo]     ").append(dir);
        logoStatus = msg.toString();
        System.out.println(logoStatus);
        return null;
    }

    private static BufferedImage foundLogo(BufferedImage img, String how) {
        logoImage = img;
        logoStatus = "[Logo] " + how.replace(File.separator + "." + File.separator, File.separator);
        System.out.println(logoStatus);
        return img;
    }

    // true for names that end like a picture (.png, .webp ...), so files such as LogoHelper.java are ignored
    private static boolean isPictureName(String fileName) {
        String name = fileName.toLowerCase(Locale.ROOT);
        for (String type : IMAGE_TYPES) if (name.endsWith(type)) return true;
        for (String type : OTHER_PICTURE_TYPES) if (name.endsWith(type)) return true;
        return false;
    }

    private static int logoRank(File file) {
        return file.getName().toLowerCase(Locale.ROOT).matches("logo\\.(png|jpg|jpeg|gif|bmp)") ? 0 : 1;
    }

    // The folder you started from, and the folders around the compiled classes (bin/ or the .jar)
    private static List<File> baseFolders() {
        List<File> bases = new ArrayList<>();
        bases.add(new File(System.getProperty("user.dir")));
        try {
            File where = new File(Logo.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            File dir = where.isDirectory() ? where : where.getParentFile();
            for (int i = 0; i < 3 && dir != null; i++) {
                bases.add(dir);
                dir = dir.getParentFile();
            }
        } catch (Exception ignored) {
            // the folder you started from is enough
        }
        return bases;
    }

    // Reads a picture; adds a plain-English reason to "problems" if it can't
    private static BufferedImage readImage(File file, List<String> problems) {
        if (!file.isFile()) return null;
        String name = file.getName().toLowerCase(Locale.ROOT);

        boolean supported = false;
        for (String type : IMAGE_TYPES) if (name.endsWith(type)) supported = true;
        if (!supported) {
            problems.add(file.getName() + " is not a type Java can open. Save it as .png or .jpg.");
            return null;
        }
        try {
            BufferedImage img = ImageIO.read(file);
            if (img == null) {
                problems.add(file.getName() + " looks like a picture but its real format is not supported "
                        + "(it may be a renamed WEBP/HEIC file). Open it and save it again as PNG.");
            }
            return img;
        } catch (IOException e) {
            problems.add(file.getName() + " could not be read: " + e.getMessage());
            return null;
        }
    }

    /**
     * Gets the logo ready at an exact pixel size.
     * A round seal (square picture) is cut into a circle; any other shape is kept whole and centered.
     */
    private static BufferedImage prepareLogo(BufferedImage src, int pixels) {
        double ratio = (double) src.getWidth() / src.getHeight();
        if (ratio > 0.9 && ratio < 1.1) {
            return circleMask(scaleSmooth(src, pixels));
        }

        int width = ratio >= 1 ? pixels : Math.max(1, (int) Math.round(pixels * ratio));
        BufferedImage fitted = scaleSmooth(src, width);
        BufferedImage canvas = new BufferedImage(pixels, pixels, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = canvas.createGraphics();
        g2.drawImage(fitted, (pixels - fitted.getWidth()) / 2, (pixels - fitted.getHeight()) / 2, null);
        g2.dispose();
        return canvas;
    }

    /** Uses the logo as the little icon of the window (title bar and taskbar). */
    public static void applyWindowIcon(Window window) {
        BufferedImage img = loadLogo();
        if (img == null) return;
        if (windowIcon == null) windowIcon = prepareLogo(img, 64);
        window.setIconImage(windowIcon);
    }

    /**
     * Shrinks an image in gentle steps (halving, then a final resize).
     * Doing it in one big jump is what makes logos look pixelated.
     */
    private static BufferedImage scaleSmooth(BufferedImage src, int targetWidth) {
        int targetHeight = Math.max(1, src.getHeight() * targetWidth / src.getWidth());
        BufferedImage img = src;
        int w = src.getWidth(), h = src.getHeight();

        while (w / 2 >= targetWidth) { // halve until we are close to the target size
            w /= 2;
            h /= 2;
            img = resize(img, w, h, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        }
        if (w != targetWidth) {
            img = resize(img, targetWidth, targetHeight, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        }
        return img;
    }

    /** Cuts the image into a circle with a smooth (anti-aliased) edge. */
    private static BufferedImage circleMask(BufferedImage src) {
        int w = src.getWidth(), h = src.getHeight();
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = out.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.WHITE);
        g2.fill(new java.awt.geom.Ellipse2D.Float(0, 0, w, h));
        g2.setComposite(AlphaComposite.SrcIn);
        g2.drawImage(src, 0, 0, null);
        g2.dispose();
        return out;
    }

    private static BufferedImage resize(BufferedImage src, int w, int h, Object interpolation) {
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = out.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interpolation);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.drawImage(src, 0, 0, w, h, null);
        g2.dispose();
        return out;
    }

    /** The school logo drawn as a smooth circle at any size. */
    public static JComponent component(int size) {
        JComponent c = new JComponent() {
            private BufferedImage cached;
            private int cachedWidth = -1;

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = Theme.smooth(g);
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                BufferedImage img = loadLogo();
                if (img != null) {
                    // render at the real screen resolution (sharper on high-DPI screens)
                    double scale = Math.max(1.0, g2.getTransform().getScaleX());
                    int pixels = (int) Math.round(getWidth() * scale);
                    if (cached == null || cachedWidth != pixels) {
                        cached = prepareLogo(img, pixels);
                        cachedWidth = pixels;
                    }
                    g2.drawImage(cached, 0, 0, getWidth(), getHeight(), null);
                } else { // simple placeholder if the image file is missing
                    g2.setColor(Theme.PRIMARY);
                    g2.fillOval(0, 0, getWidth(), getHeight());
                    g2.setColor(Theme.GOLD);
                    g2.fillOval(getWidth() / 4, getHeight() / 4, getWidth() / 2, getHeight() / 2);
                }
                g2.dispose();
            }
        };
        Dimension d = new Dimension(size, size);
        c.setPreferredSize(d);
        c.setMinimumSize(d);
        c.setMaximumSize(d);
        return c;
    }
}
