import com.company.core.utils.D;
import com.company.core.utils.ProxyUtils;
import com.work.app.app360.App360;
import com.company.core.App;
import com.work.proxyIp.kuai_proxy.AppKuai;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {
    public static Logger log = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        ProxyUtils.init();

        App app = null;
        long time = System.currentTimeMillis();
        app = new App360();
//        app = new AppKuai();
        app.start();

        if (app.finish());

        ProxyUtils.debug();
//        if(app.finish()==0) {
//            System.out.println("success end  "+(System.currentTimeMillis()-time));
//        }
        System.out.println("==>end  "+(System.currentTimeMillis()-time));
    }
}


