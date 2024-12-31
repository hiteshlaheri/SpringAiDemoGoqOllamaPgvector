package in.mbs.aitool.AITooldemo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class ReportService {

    private String sql= """
            INSERT INTO public.report(
            	id, description, name, reportcategory, reportsubcategory)
            	VALUES ('1','finance report, which have data of accounting','Account Report','Finance','Account');
                        
            INSERT INTO public.report(
            	id, description, name, reportcategory, reportsubcategory)
            	VALUES ('2','Operation report, which have data of warehouse operation','Stock Report','Operation','Stock');
                        
            INSERT INTO public.report(
            	id, description, name, reportcategory, reportsubcategory)
            	VALUES ('3','Stock By report, which have data of Stocks','Stock By Warehouse Report','Operation','Warehouse');
            INSERT INTO public.report(
            	id, description, name, reportcategory, reportsubcategory)
            	VALUES ('4','invoice report, which have data of invoice','Invoice Report','Finance','Invoice');
            """;
    private final JdbcTemplate jdbcTemplate;

    public ReportService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    public void executeSqlFromFile() {
        System.out.println("SQL content: " + sql);

        // Execute SQL if needed
        jdbcTemplate.execute(sql);
    }
}
