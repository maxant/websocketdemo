package ch.maxant.websocketdemo.aro.data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "T_TASK")
@NamedQueries({
        @NamedQuery(name = Task.NQFindByCaseNumber.NAME, query = Task.NQFindByCaseNumber.QUERY)
})
public class Task {

    public static class NQFindByCaseNumber {
        public static final String NAME = "Task.findByCaseNumber";
        public static final String PARAM_NR = "caseNr";
        public static final String QUERY = "select t from Task t where t.caseNr = :" + PARAM_NR;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "CASE_NR", nullable = false)
    private long caseNr;

    @Column(name = "DESCRIPTION", length = 100)
    private String description;

    @Column(name = "CREATED")
    private LocalDateTime created;

    @PrePersist
    public void prePersist(){
        this.created = LocalDateTime.now();
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public String getDescription() {
        return description;
    }

    public long getId() {
        return id;
    }

    public long getCaseNr() {
        return caseNr;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCaseNr(long caseNr) {
        this.caseNr = caseNr;
    }
}
