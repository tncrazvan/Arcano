package com.github.tncrazvan.arcano.Tool;

import com.github.tncrazvan.arcano.Configuration;
import static com.github.tncrazvan.arcano.SharedObject.RUNTIME;
import static com.github.tncrazvan.arcano.Tool.Encoding.JsonTools.jsonArray;
import com.google.gson.JsonArray;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Administrator
 */
public class Minifier{
    private final String inputDirName;
    private final File assets;
    private final String outputDirectoryname = "minified";
    private final String outputFilename = "minified";
    private final File minifiedJS;
    private final File minifiedCSS;
    private final Configuration config;
    private File dir;

    public Minifier(Configuration config, final File assetsFile, final String inputDirName, final String outputSubDirName) throws IOException {
        new HashMap<>();
        this.assets = assetsFile;
        this.inputDirName = inputDirName;
        dir = new File(inputDirName + outputDirectoryname);
        minifiedJS = new File(inputDirName + outputDirectoryname + "/" + outputFilename + ".js");
        minifiedCSS = new File(inputDirName + outputDirectoryname + "/" + outputFilename + ".css");
        this.config = config;
    }

    public static final byte[] minify(Configuration config, final byte[] content, final String type) throws IOException {
        return minify(config, content, type, Thread.currentThread().getId() + "");
    }

    public static final byte[] minify(Configuration config, final byte[] content, final String type, final String hashCode) throws IOException {
        File tmp = new File("tmp");
        if (!tmp.exists())
            tmp.mkdir();
        else if (!tmp.isDirectory()) {
            tmp.delete();
            tmp.mkdir();
        }
        String filename = "./tmp/." + hashCode + "." + type + ".minified.input.tmp".replace("\\", "/").replace("//","/");
        tmp = new File(config.dir,filename); 
        //System.out.println("Creating new file: "+filename);
        //System.out.println("Content length: "+content.length);
        if (tmp.exists())
            tmp.delete();
        tmp.mkdirs();
        tmp.createNewFile();
        FileOutputStream fos = new FileOutputStream(tmp);
        fos.write(content);
        fos.close();
        //System.out.println("Tried to write to file, file is now "+tmp.length()+" bytes in size.");
        Process process;
        // Script example: minify --type=@type \"@filename\"
        // Using https://github.com/tdewolff/minify
        final String script = Regex.replace(Regex.replace(config.pack.script, "\\@type", type), "\\@filename", filename);
        //System.out.println("Executing: "+script);
        process = RUNTIME.exec(script, new String[]{}, new File(config.dir));
        final byte[] result = process.getInputStream().readAllBytes();
        //System.out.println("Read "+result.length+" bytes.");
        process.destroy();
        //System.out.println("Maintaining file...");
        return result;
    }

    private String js = "";
    private String css = "";
    private File f;
    private FileInputStream fis;
    private int size;
    private String filename;
    private String[] filenames;
    private final String REGEX_PATTERN_CALL = "\\/+\\$regex[:\\s].*";
    private final String REGEX_PATTERN_CALL_VALUE = "(?<=\\/+\\$regex[:\\s]).*";
    Pattern pattern;
    Matcher matcher;

    public final void minify() throws IOException {
        minify(true);
    }

    public final void minify(final boolean min) throws IOException {
        js = "";
        css = "";
        JsonArray arr = new JsonArray();
        try (FileInputStream fis = new FileInputStream(assets)) {
            arr = jsonArray(new String(fis.readAllBytes()));
            fis.close();
        }
        size = arr.size();
        try {
            for (int i = 0; i < size; i++) {
                filename = inputDirName + arr.get(i).getAsString();
                pattern = Pattern.compile(REGEX_PATTERN_CALL);
                matcher = pattern.matcher(filename);
                if (matcher.find()) {
                    final ArrayList<String> tmpFilenames = new ArrayList<>();
                    final String dirname = filename.replaceAll(REGEX_PATTERN_CALL, "");
                    dir = new File(dirname);
                    if (!dir.isDirectory())
                        continue;
                    pattern = Pattern.compile(REGEX_PATTERN_CALL);
                    matcher = pattern.matcher(filename);
                    if (!matcher.find())
                        continue;
                    // gets the first match only, I don't care about the rest
                    final String regexCall = matcher.group();
                    pattern = Pattern.compile(REGEX_PATTERN_CALL_VALUE);
                    matcher = pattern.matcher(regexCall);
                    if (!matcher.find())
                        continue;
                    final String regexValue = matcher.group();
                    pattern = Pattern.compile(regexValue);
                    for (final String listedFilename : dir.list()) {
                        matcher = pattern.matcher(listedFilename);
                        if (!matcher.find())
                            continue;
                        tmpFilenames.add(dir + "/" + listedFilename);
                    }
                    filenames = tmpFilenames.toArray(filenames);
                } else {
                    if (!filename.endsWith(".js") && !filename.endsWith(".css") && !filename.endsWith(".html")
                            && !filename.endsWith(".htm"))
                        continue;
                    filenames = new String[] { filename };
                }

                for (final String listedFilename : filenames) {
                    if (listedFilename == null)
                        continue;
                    f = new File(listedFilename);
                    fis = new FileInputStream(f);
                    //System.out.println("Loading filename: "+listedFilename);
                    if (listedFilename.endsWith(".js")) {
                        js += min ? new String(minify(config, fis.readAllBytes(), "js", this.hashCode() + ""))
                                : new String(fis.readAllBytes());
                    } else if (listedFilename.endsWith(".css")) {
                        css += min ? new String(minify(config, fis.readAllBytes(), "css", this.hashCode() + ""))
                                : new String(fis.readAllBytes());
                    }

                    fis.close();
                }

            }
        } catch (final IOException e) {
            e.printStackTrace(System.out);
        }

        fis = null;

        save(dir, minifiedJS, js.getBytes());
        save(dir, minifiedCSS, css.getBytes());
    }

    private final void save(final File dir, final File minified, final byte[] contents) {
        try {
            if (!dir.exists())
                dir.mkdir();

            if (minified.exists())
                minified.delete();
            minified.createNewFile();
            try (FileOutputStream fos = new FileOutputStream(minified)) {
                fos.write(contents);
                fos.close();
            }
        } catch (final IOException e) {
        }
    }
}
 