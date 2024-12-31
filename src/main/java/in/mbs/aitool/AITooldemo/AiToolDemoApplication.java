package in.mbs.aitool.AITooldemo;

import in.mbs.aitool.AITooldemo.Repository.ReportRepository;
import in.mbs.aitool.AITooldemo.data.Report;
import in.mbs.aitool.AITooldemo.service.ReportService;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.PgVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SpringBootApplication
public class AiToolDemoApplication {
	@Autowired
	ReportService reportService;
	@Autowired
	PgVectorStore pgVectorStore;
	@Autowired
	ReportRepository repository;
	public static void main(String[] args) {
		SpringApplication.run(AiToolDemoApplication.class, args);
	}


	@Bean
	public CommandLineRunner commandLineRunner(JdbcTemplate jdbcTemplate){
		return args -> {
			if(jdbcTemplate.queryForObject("""
					select count(*)
					from report
					""",Integer.class)==0){
				reportService.executeSqlFromFile();
			}

			if(jdbcTemplate.queryForObject("""
					select count(*)
					from vector_store
					""",Integer.class)==0){
				List<Report> reportList = repository.findAll();
				List<Document> documentList = new ArrayList<>();
				reportList.forEach(report -> {
					String content = "Name="+report.getName()+" Description="+report.getDescription() + " Report Category="+report.getReportcategory()
							+" Report Sub category="+report.getReportsubcategory();
					documentList.add(new Document(content, Map.of("ID",report.getId(),"Name",report.getName(),"Description",report.getDescription()
							,"Report Category",report.getReportcategory(),"Report Sub category",report.getReportsubcategory())));

				});
				pgVectorStore.add(documentList);
			}

		} ;
	}

}
