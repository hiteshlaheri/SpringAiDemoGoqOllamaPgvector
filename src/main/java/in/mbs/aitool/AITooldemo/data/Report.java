package in.mbs.aitool.AITooldemo.data;

import jakarta.persistence.*;

@Entity
@Table(name = "report")
public class Report{
    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getReportcategory() {
        return reportcategory;
    }

    public void setReportcategory(String reportcategory) {
        this.reportcategory = reportcategory;
    }

    public String getReportsubcategory() {
        return reportsubcategory;
    }

    public void setReportsubcategory(String reportsubcategory) {
        this.reportsubcategory = reportsubcategory;
    }

    @Id
    String Id;
    @Column
    String Name;
    @Column
    String Description;
    @Column
    String reportcategory;
    @Column
    String reportsubcategory;
}