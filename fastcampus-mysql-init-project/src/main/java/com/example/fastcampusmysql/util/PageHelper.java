package com.example.fastcampusmysql.util;

import org.springframework.data.domain.Sort;

import java.util.List;

public class PageHelper {
    public static String orderBy(Sort sort) {
        // Sort -> isEmpty()라는 함수가 있음
        //  -> 그래서 Sort가 없을 때 기본처리를 해줄 필요가 있다!

        if(sort.isEmpty()) {
            return "id DESC";
        }

        // sort에 필드가 여러 개면 order by id desc, contents desc 이렇게 풀려야 함
        //  -> id desc나 contents desc 같은 하나하나가 Sort 객체에서는 Order
        //  -> Order라는 객체를 이용해서 List<Order>를 순회하면서 String을 하나하나 만들어주면 됨
        //  -> String을 만들어주는 것은 Order의 property와 direction이 될 것임!

        List<Sort.Order> orders = sort.toList();
        var orderBys = orders.stream()
                .map(order -> order.getProperty() + " " + order.getDirection())
                .toList();

        return String.join(", ", orderBys);
    }
}
