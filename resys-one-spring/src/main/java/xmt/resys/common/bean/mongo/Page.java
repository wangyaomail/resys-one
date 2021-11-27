package xmt.resys.common.bean.mongo;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Page<T> implements Pageable {
    private int pageNumber; // 请求的页码
    private int pageSize = 10; // 每页显示多少个
    private int totalSize;
    private int totalPage;
    private Sort sort;
    private List<?> list = new ArrayList<>(); // 记录当前页中的数据条目
                                              // 注意这里的类型是问号的原因是为了避免查询类型的相互影响

    public Page(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    @Override
    public Pageable first() {
        return null;
    }

    @Override
    public Pageable withPage(int pageNumber) {
        return null;
    }

    @Override
    public long getOffset() {
        return (getPageNumber() - 1) * getPageSize();
    }

    @Override
    public boolean hasPrevious() {
        return false;
    }

    @Override
    public Pageable next() {
        return null;
    }

    @Override
    public Pageable previousOrFirst() {
        return null;
    }

    public void setTotalSize(int totalSize) {
        this.totalSize = totalSize;
        this.totalPage = totalSize % pageSize == 0 ? totalSize / pageSize
                : totalSize / pageSize + 1;
    }
}
