package com.example.toby.jiw.common.config;

import com.example.toby.jiw.common.config.annotaion.EnableSqlService;
import com.example.toby.jiw.dao.UserDao;
import com.example.toby.jiw.dao.UserDaoJdbc;
import com.example.toby.jiw.dao.sql.*;
import com.example.toby.jiw.service.DummyMailSender;
import com.example.toby.jiw.service.TestUserServiceImpl;
import com.example.toby.jiw.service.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.mail.MailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@ComponentScan(basePackages = "com.example.toby.jiw.dao")
@EnableTransactionManagement
@EnableSqlService
//@PropertySource("/application.properties")    // ApplicationContext 에러로 인한 미실시
//@Import(SqlServiceContext.class)  ch7 680p~ @Import 및 @Profile 전부 ApplicationContext 에러로 인한 미실시
public class AppContext implements SqlMapConfig {

    @Autowired UserDao userDao;

    @Bean
    public UserDaoJdbc userDao() {
        return new UserDaoJdbc(sqlService(), dataSource());
    }

    @Bean
    public DataSource dataSource() {
        return new SingleConnectionDataSource("jdbc:h2:tcp://localhost/~/test", "sa", "", true);    // ch6 aop @Transactional 을 사용한 DB테스트 시 H2는 실패하는 문제발생
//        return new SingleConnectionDataSource("jdbc:mysql://localhost:3306/sys?serverTimezone=UTC&characterEncoding=UTF-8", "root", "1234", true);
    }

    @Bean
    public UserServiceImpl userService() {
        return new UserServiceImpl(userDao(), mailSender());
    }

    @Bean
    public MailSender mailSender() {    //
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("mail.mycompany.com");
        return mailSender;
    }

    @Bean
    public DataSourceTransactionManager transactionManager() {  // dataSource 의 커넥션을 가져와 트랜잭션 처리해야 하기에 DataSource 를 받음
        return new DataSourceTransactionManager(dataSource());
    }

    // ch7

//    @Bean
//    public SqlMapConfig sqlMapConfig() {
//        return new UserSqlMapConfig();
//    }

    @Override
    public Resource getSqlMapResource() {
        return new ClassPathResource("/sql/sqlmap.xml", UserDao.class);
    }

    @Bean
    public OxmSqlService sqlService() {
        OxmSqlService sqlService = new OxmSqlService();
        sqlService.setUnmarshaller(unmarshaller());
        sqlService.setSqlRegistry(sqlRegistry());
        sqlService.setSqlmap(getSqlMapResource());
        return sqlService;
    }

    @Bean
    public JaxbXmlSqlReader sqlReader() {
        JaxbXmlSqlReader reader = new JaxbXmlSqlReader();
        reader.setSqlmapFile("/sql/sqlmap.xml");
        return reader;
    }

    @Bean
    public EmbeddedDbSqlRegistry sqlRegistry() {
        EmbeddedDbSqlRegistry sqlRegistry = new EmbeddedDbSqlRegistry();
        sqlRegistry.setDataSource(embeddedDatabase());
        return sqlRegistry;
    }

    @Bean
    public Jaxb2Marshaller unmarshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPath("com.example.toby.jiw.dao.sql.jaxb");
        return marshaller;
    }

    @Bean
    public DataSource embeddedDatabase() {
        return new EmbeddedDatabaseBuilder()
                .setName("embeddedDatabase")
                .setType(EmbeddedDatabaseType.HSQL)
                .addScript("/sql/embedded-db-schema.sql")
                .build();
    }

//    @Configuration
//    @Profile("production")
//    public static class ProductionAppContext {
//        @Bean
//        public MailSender mailSender() {    //  운영용 메일
//            JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
//            mailSender.setHost("localhost");
//            return mailSender;
//        }
//    }
//
//    @Configuration
//    @Profile("test")
//    public static class TestAppContext {
//        @Autowired
//        UserDao userDao;
//
//        @Bean
//        public TestUserServiceImpl testUserService() {
//            return new TestUserServiceImpl(userDao, mailSender());
//        }
//
//        @Bean
//        public MailSender mailSender() {
//            return new DummyMailSender();
//        }
//    }
}
