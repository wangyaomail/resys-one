package xmt.resys.common.bean.http;

import com.google.common.cache.CacheStats;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用于缓存库cache状况的查看类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HBCacheStats {
    private Long hitCount; // 命中数量
    private Long missCount; // 没有命中的数量
    private Double hitRate; // 命中率
    private Double missRate; // 没有命中率
    private Long loadSuccessCount; // 从数据库加载数据成功的条数
    private Long loadExceptionCount; // 从数据库加载数据失效的条数
    private Long totalLoadTime; // 从数据库中加载数据总共消耗的时间
    private Long evictionCount; // 缓存逐出的数量
    private Double averageLoadPenalty; // 加载新值所花费的平均时间

    public static HBCacheStats fromStats(CacheStats src) {
        HBCacheStats tar = new HBCacheStats();
        if (src != null) {
            tar.setHitCount(src.hitCount());
            tar.setMissCount(src.missCount());
            tar.setHitRate(src.hitRate());
            tar.setMissRate(src.missRate());
            tar.setLoadSuccessCount(src.loadSuccessCount());
            tar.setLoadExceptionCount(src.loadExceptionCount());
            tar.setTotalLoadTime(src.totalLoadTime());
            tar.setEvictionCount(src.evictionCount());
            tar.setAverageLoadPenalty(src.averageLoadPenalty());
        }
        return tar;
    }
}
