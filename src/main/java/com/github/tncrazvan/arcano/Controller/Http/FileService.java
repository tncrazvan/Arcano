package com.github.tncrazvan.arcano.Controller.Http;

import com.github.tncrazvan.arcano.Http.HttpController;
import java.io.IOException;
import com.github.tncrazvan.arcano.Bean.Http.HttpDefault;
import java.io.File;

/**
 *
 * @author Administrator
 */
public class FileService extends HttpController {
    @HttpDefault
    public File main() throws IOException, ClassNotFoundException {
        if(reader.args.length == 0) 
            reader.args = new String[]{reader.so.config.entryPoint};
        else if(reader.args.length == 1 && reader.args[0].equals("")) 
            reader.args[0] = reader.so.config.entryPoint;
        return new Delegate<Get>(){}.start().file();
    }
}
