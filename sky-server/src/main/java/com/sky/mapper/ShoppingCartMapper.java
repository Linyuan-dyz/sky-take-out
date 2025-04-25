package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {

    List<ShoppingCart> list(ShoppingCart shoppingCart);

    void insert(ShoppingCart shoppingCart);

    void updateNumber(ShoppingCart shoppingCart);

    void clean(ShoppingCart shoppingCart);

    void remove(ShoppingCart shoppingCart);
}
