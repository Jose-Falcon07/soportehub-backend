package pa.soportehub.application.query;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;

public record PageResult<T>(List<T> content, int page, int size, long total) {

    @JsonIgnore
    public int totalPages() {
        return (int) Math.ceil((double) total / size);
    }
}
