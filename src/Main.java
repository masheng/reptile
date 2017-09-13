import com.books.dangdang.Dang;
import com.company.core.utils.D;
import com.work.app.app360.App360;
import com.company.core.App;
import com.work.app.baidu.AppBaidu;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {
    public static Logger log = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        App app = null;
        long time = System.currentTimeMillis();
//        app = new App360();
        app = new AppBaidu();
        app.start();

        if(app.finish()) {
            System.out.println("success end  "+(System.currentTimeMillis()-time));
        }
        System.out.println("==>end  "+(System.currentTimeMillis()-time));
    }
}


