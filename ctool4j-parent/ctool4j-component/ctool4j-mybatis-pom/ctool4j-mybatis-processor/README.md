# AutoBizService 注解处理器

基于 Java 注解处理器，编译时自动生成业务 Service 接口。

## 功能说明

参照 `ICBizService` 的 `biz_id` 方法模式，自动生成如 `IOrderService`、`IVoucherService` 等业务 Service 接口。

## 使用方法

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.c332030</groupId>
    <artifactId>ctool4j-mybatis-processor</artifactId>
    <scope>provided</scope>
</dependency>
```

### 2. 在实体类上添加注解

```java
import com.c332030.ctool4j.mybatisplus.processor.AutoBizService;
import com.c332030.ctool4j.definition.entity.base.CBaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@AutoBizService(bizIdField = "orderNo", bizIdColumn = "order_no")
public class Order extends CBaseEntity<Long> {
    
    private String orderNo;
    
    private String productName;
    
    private Integer quantity;
}
```

### 3. 编译后自动生成接口

编译后会自动生成 `IOrderService` 接口：

```java
public interface IOrderService<ENTITY extends Order> extends ICBizService<ENTITY, Order> {

    String getOrderNo(Order entity);
    
    SFunction<ENTITY, String> getOrderNoColumn();
    
    default ENTITY getByOrderNo(String orderNo) { ... }
    
    default List<ENTITY> listByOrderNo(String orderNo) { ... }
    
    default Long countByOrderNo(String orderNo) { ... }
    
    default boolean updateByOrderNo(ENTITY entity) { ... }
    
    default boolean removeByOrderNo(String orderNo) { ... }
    
    default List<ENTITY> listByOrderNos(Collection<String> orderNos) { ... }
    
    default Map<String, ENTITY> listMapByOrderNos(Collection<String> orderNos) { ... }
    
    // ... 更多方法
}
```

### 4. 实现接口

```java
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.c332030.ctool4j.mybatisplus.service.impl.CServiceImpl;
import com.c332030.ctool4j.mybatisplus.mapper.CBaseMapper;

public class OrderServiceImpl<M extends CBaseMapper<Order>>
        extends CServiceImpl<M, Order>
        implements IOrderService<Order> {

    @Override
    public String getOrderNo(Order entity) {
        return entity.getOrderNo();
    }

    @Override
    public SFunction<Order, String> getOrderNoColumn() {
        return Order::getOrderNo;
    }

    @Override
    public String getBizId(Order entity) {
        return getOrderNo(entity);
    }

    @Override
    public SFunction<Order, String> getBizIdColumn() {
        return getOrderNoColumn();
    }
}
```

### 5. 自动生成实现类（可选）

如果设置 `generateImpl = true`：

```java
@AutoBizService(bizIdField = "orderNo", bizIdColumn = "order_no", generateImpl = true)
public class Order extends CBaseEntity<Long> {
    // ...
}
```

会额外自动生成 `OrderServiceImpl` 实现类。

## 注解参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `bizIdField` | String | 是 | 业务ID字段名（Java字段名，如 `orderNo`） |
| `bizIdColumn` | String | 否 | 业务ID数据库列名，默认使用 `bizIdField` 的下划线形式 |
| `serviceSuffix` | String | 否 | 生成的接口名后缀，默认 `Service` |
| `generateImpl` | boolean | 否 | 是否生成实现类，默认 `false` |

## 多个业务ID示例

如果实体有多个业务ID，可以使用多次（但建议拆分为多个实体）：

```java
@Data
@AutoBizService(bizIdField = "voucherNo", bizIdColumn = "voucher_no")
public class Voucher extends CBaseEntity<Long> {
    
    private String voucherNo;
    
    private String voucherName;
}
```

## 原理

- 使用 Java 注解处理器（Annotation Processor）
- 编译时扫描 `@AutoBizService` 注解
- 根据实体类名和业务ID字段自动生成 Service 接口
- 生成的代码与手写代码完全一致，IDE 完全支持

## 与 Lombok 的关系

类似 Lombok 的实现原理，但：
- Lombok 通过修改 AST 生成代码
- 本处理器通过创建新的源文件生成代码
- 两者可以同时使用
