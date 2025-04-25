package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private DishMapper dishMapper;

    @Override
    public List<ShoppingCart> list() {
        //获取当前用户id
        Long userId = BaseContext.getCurrentId();

        //构建对应购物车实例
        ShoppingCart shoppingCart =  ShoppingCart
                                    .builder()
                                    .userId(userId)
                                    .build();

        //由于测试时无法使用微信小程序，所以只能通过api发送请求，无法获取userId，所以设置为1
        setUserIdInApi(shoppingCart);

        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        return list;
    }

    @Override
    public void insert(ShoppingCartDTO shoppingCartDTO) {
        //获取当前用户id
        Long userId = BaseContext.getCurrentId();

        //获取当前用户的购物车内容
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        shoppingCart.setUserId(userId);

        //由于测试时无法使用微信小程序，所以只能通过api发送请求，无法获取userId，所以设置为1
        setUserIdInApi(shoppingCart);

        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        //判断当前用户购物车内是否存在对应菜品或套餐
        if (list != null && list.size() > 0) {
            //如果存在，则对应菜品或套餐数量+1
            shoppingCart = list.get(0);
            shoppingCart.setNumber(shoppingCart.getNumber() + 1);
            shoppingCartMapper.updateNumber(shoppingCart);
        } else {
            //如果不存在，则加入该菜品或套餐
            //首先区分到底是菜品还是套餐，通过dishId判断，如果存在则为菜品，否则为套餐
            Long dishId = shoppingCartDTO.getDishId();
            if (dishId != null) {
                //此时为菜品
                Dish dish = dishMapper.getById(dishId);
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice());
            } else {
                //此时为套餐
                Setmeal setmeal = new Setmeal();
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setAmount(setmeal.getPrice());
            }
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.insert(shoppingCart);
        }

    }

    @Override
    public void clean() {
        //获取当前用户id
        Long userId = BaseContext.getCurrentId();

        ShoppingCart shoppingCart = new ShoppingCart();

        //由于测试时无法使用微信小程序，所以只能通过api发送请求，无法获取userId，所以设置为1
        setUserIdInApi(shoppingCart);

        shoppingCartMapper.clean(shoppingCart);
    }

    @Override
    public void sub(ShoppingCartDTO shoppingCartDTO) {

        //获取当前用户id
        Long userId = BaseContext.getCurrentId();

        //获取当前用户的购物车内容
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        shoppingCart.setUserId(userId);

        //由于测试时无法使用微信小程序，所以只能通过api发送请求，无法获取userId，所以设置为1
        setUserIdInApi(shoppingCart);

        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        //判断当前用户购物车内是否存在对应菜品或套餐
        if (list != null && list.size() > 0) {
            //如果存在，则对应菜品或套餐数量-1
            shoppingCart = list.get(0);
            Integer number = shoppingCart.getNumber();
            if (number == 1) {
                shoppingCartMapper.remove(shoppingCart);
            } else {
                shoppingCart.setNumber(number - 1);
                shoppingCartMapper.updateNumber(shoppingCart);
            }

        } else {
            //如果不存在，则报错
            throw new DeletionNotAllowedException(MessageConstant.SUB_NOT_FOUND);
        }
    }

    public void setUserIdInApi(ShoppingCart shoppingCart) {
        if (shoppingCart.getUserId() == null) {
            shoppingCart.setUserId(1L);
        }
    }
}
