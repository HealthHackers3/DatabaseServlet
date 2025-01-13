package api.post.get;

import api.interfaces.apiCommandHandler;
import util.userAuthenticator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Statement;

public class getImageModalities implements apiCommandHandler {
    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp, Statement s) throws Exception {
        if(!userAuthenticator.checkSession(req, resp, s.getConnection())){return;}
        //return the categories in alphabetical order
        s.execute("SELECT * FROM limage_modalities WHERE image_modality_name != 'Other' ORDER BY image_modality_name");
        statement2Json(req,resp,s);
    }
}
