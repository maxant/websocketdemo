package ch.maxant.websocketdemo.mcs.data;

import javax.persistence.*;

@Entity
@Table(name = "T_COMMAND")
@NamedQueries({
        @NamedQuery(name = Command.NQSelectAll.NAME, query = Command.NQSelectAll.QUERY)
})
public class Command {

    public static class NQSelectAll {
        public static final String NAME = "Command.selectAll";
        public static final String QUERY = "select c from Command c";
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "COMMAND", nullable = false)
    private String command;

    @Column(name = "VERSION", nullable = false)
    @Version
    private int version;

    public long getId() {
        return id;
    }

    public String getCommand() {
        return command;
    }
}
