package api.post.get;

import api.interfaces.apiCommandHandler;
import util.userAuthenticator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Statement;

public class getCellTypes implements apiCommandHandler {
    @Override
    public void handle(HttpServletRequest req, HttpServletResponse resp, Statement s) throws Exception {
        if(!userAuthenticator.checkSession(req, resp, s.getConnection())){return;}
        //return the celltypes in alphabetical order
        s.execute("SELECT * FROM lcell_types WHERE cell_type_name != 'Other' ORDER BY cell_type_name");
        statement2Json(req,resp,s);
    }
}
