package pl.javastart.couponcalc;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public class PriceCalculator {

    public double calculatePrice(List<Product> products, List<Coupon> coupons) {

        if (products == null) {
            return 0;
        } else if (coupons == null) {
            return calcRegularPrice(products);
        } else {
            return calcDiscountPrice(products, coupons);
        }

//        return 0;
    }

    private double calcDiscountPrice(List<Product> products, List<Coupon> coupons) {
        EnumMap<Category, Double> priceMap = getPricesMap(products);
        EnumMap<Category, Integer> discountMap = getCouponsMap(coupons);

        BigDecimal orderPrice;
        List<Double> pricesWithDiscounts = new ArrayList<>();

        if (discountMap.containsKey(Category.NO_CATEGORY)) {
            orderPrice = getFinalPriceWithNoCategoryCoupon(products, discountMap).setScale(2, RoundingMode.HALF_UP);
            pricesWithDiscounts.add(orderPrice.doubleValue());
        }

        for (Map.Entry<Category, Double> entry : priceMap.entrySet()) {
            getReducedPrices(priceMap, discountMap, pricesWithDiscounts, entry);
        }

        if (pricesWithDiscounts.size() == 0) {
            pricesWithDiscounts.add(calcRegularPrice(products));
        }

        return pricesWithDiscounts.stream()
                .mapToDouble(x -> x)
                .min()
                .orElseThrow(NoSuchElementException::new);
    }

    private static void getReducedPrices(EnumMap<Category, Double> priceMap, EnumMap<Category, Integer> discountMap,
            List<Double> pricesWithDiscounts, Map.Entry<Category, Double> entry) {
        BigDecimal orderPrice;
        Category productCategory = entry.getKey();
        double price = entry.getValue();

        if (discountMap.containsKey(productCategory)) {
            int discount = discountMap.get(productCategory);
            price = price * (100 - discount) / 100;

            EnumMap<Category, Double> reducedMap = new EnumMap<>(priceMap);
            reducedMap.remove(productCategory);
            double pricesWithoutCoupon = reducedMap.values().stream().mapToDouble(x -> x).sum();

            orderPrice = BigDecimal.valueOf(price + pricesWithoutCoupon).setScale(2, RoundingMode.HALF_UP);
            pricesWithDiscounts.add(orderPrice.doubleValue());
        }
    }

    private BigDecimal getFinalPriceWithNoCategoryCoupon(List<Product> products, EnumMap<Category, Integer> discountMap) {
        double price;

        price = calcRegularPrice(products) * (100 - discountMap.get(Category.NO_CATEGORY)) / 100;
        return BigDecimal.valueOf(price);
    }

    private static EnumMap<Category, Double> getPricesMap(List<Product> products) {
        EnumMap<Category, Double> priceMap = new EnumMap<>(Category.class);
        for (Product product : products) {
            if (priceMap.containsKey(product.getCategory())) {
                double sum = product.getPrice() + priceMap.get(product.getCategory());
                priceMap.put(product.getCategory(), sum);
            } else {
                priceMap.put(product.getCategory(), product.getPrice());
            }
        }
        return priceMap;
    }

    private static EnumMap<Category, Integer>  getCouponsMap(List<Coupon> coupons) {
        EnumMap<Category, Integer> discountMap = new EnumMap<>(Category.class);
        int discountValue;
        for (Coupon coupon : coupons) {
            if (coupon.getCategory() != null) {
                discountValue = getDiscountValue(discountMap, coupon, coupon.getCategory());
                discountMap.put(coupon.getCategory(), discountValue);
            } else {
                discountValue = getDiscountValue(discountMap, coupon, Category.NO_CATEGORY);
                discountMap.put(Category.NO_CATEGORY, discountValue);
            }
        }
        return discountMap;
    }

    private static int getDiscountValue(EnumMap<Category, Integer> discountMap, Coupon coupon, Category category) {
        int discountValue;
        if (discountMap.containsKey(category)) {
            discountValue = Math.max(coupon.getDiscountValueInPercents(), discountMap.get(category));
        } else {
            discountValue = coupon.getDiscountValueInPercents();
        }
        return discountValue;
    }

    private double calcRegularPrice(List<Product> products) {
        double orderPrice = 0.0;
        for (Product product : products) {
            orderPrice += product.getPrice();
        }
        return  orderPrice;
    }

}
