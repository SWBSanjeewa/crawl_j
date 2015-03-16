package versioneye.mojo;


import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import versioneye.domain.GlobalSetting;
import versioneye.domain.MavenRepository;
import versioneye.domain.Repository;
import versioneye.maven.MavenPomProcessor;
import versioneye.maven.MavenProjectProcessor;
import versioneye.persistence.IGlobalSettingDao;
import versioneye.persistence.IMavenRepostoryDao;
import versioneye.persistence.IProductDao;
import versioneye.service.ProductService;

@Mojo( name = "repo1index", defaultPhase = LifecyclePhase.PROCESS_SOURCES )
public class Repo1IndexMojo extends CentralMojo {

    private String username;
    private String password;

    public void execute() throws MojoExecutionException, MojoFailureException {
        try{
            ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");

            productDao = (IProductDao) context.getBean("productDao");
            mavenRepositoryDao = (IMavenRepostoryDao) context.getBean("mavenRepositoryDao");
            globalSettingDao = (IGlobalSettingDao) context.getBean("globalSettingDao");

            productService = (ProductService) context.getBean("productService");

            mavenProjectProcessor = (MavenProjectProcessor) context.getBean("mavenProjectProcessor");
            mavenPomProcessor = (MavenPomProcessor) context.getBean("mavenPomProcessor");

            fetchUserAndPassword();
            String baseUrl = fetchBaseUrl();
            Repository repository = repositoryUtils.convertRepository("MavenInternal", baseUrl, null);
            repository.setUsername(username);
            repository.setPassword(password);
            mavenProjectProcessor.setRepository(repository);
            mavenPomProcessor.setRepository(repository);

            mavenRepository = new MavenRepository();
            mavenRepository.setName(repository.getName());
            mavenRepository.setUrl(repository.getSrc());
            mavenRepository.setLanguage("Java");
            addRepo(mavenRepository);

            super.doUpdateFromIndex();
        } catch( Exception exception ){
            getLog().error(exception);
            throw new MojoExecutionException("Oh no! Something went wrong. Get in touch with the VersionEye guys and give them feedback.", exception);
        }
    }

    private String fetchBaseUrl(){
        String env = System.getenv("RAILS_ENV");
        getLog().info("fetchBaseUrl for env: " + env );
        try{
            GlobalSetting gs = globalSettingDao.getBy(env, "mvn_repo_1");
            String url = gs.getValue();
            getLog().info(" - mvn_repo_1: " + url);
            return url;
        } catch( Exception ex){
            ex.printStackTrace();
            return "http://repo.maven.apache.org/maven2";
        }
    }

    private void fetchUserAndPassword(){
        String env = System.getenv("RAILS_ENV");
        try{
            username = globalSettingDao.getBy(env, "mvn_repo_1_user").getValue();
            password = globalSettingDao.getBy(env, "mvn_repo_1_password").getValue();
        } catch( Exception ex){
            ex.printStackTrace();
            username = "admin";
            password = "password";
        }
    }

}