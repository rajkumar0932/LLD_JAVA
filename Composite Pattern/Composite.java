import java.util.*;

// COMPONENT: the shared interface — both a single item and a group implement it,
// so the client can treat them identically.
interface CartItem {
    double getPrice();
    void display(String indent);
}

// LEAF: an atomic object. Has no children.
class Product implements CartItem {
    private String name;
    private double price;

    public Product(String name, double price) {
        this.name = name;
        this.price = price;
    }

    @Override
    public double getPrice() {
        return price;
    }

    @Override
    public void display(String indent) {
        System.out.println(indent + "Product: " + name + " - Rs " + price);
    }
}

// COMPOSITE: a container. Holds CartItems — which can be Products (leaves)
// OR other ProductBundles (nested composites). That recursion is the whole point.
class ProductBundle implements CartItem {
    private String bundleName;
    private List<CartItem> items = new ArrayList<>();

    public ProductBundle(String bundleName) {
        this.bundleName = bundleName;
    }

    public void addItem(CartItem item) {   // accepts ANY CartItem — leaf or composite
        items.add(item);
    }

    @Override
    public double getPrice() {
        double total = 0;
        for (CartItem item : items) {      // delegate to children, no type checks
            total += item.getPrice();
        }
        return total;
    }

    @Override
    public void display(String indent) {
        System.out.println(indent + "Bundle: " + bundleName);
        for (CartItem item : items) {
            item.display(indent + "  ");    // recurses naturally for nested bundles
        }
    }
}

public class Composite {
    public static void main(String[] args) {
        // Individual products (leaves)
        CartItem book    = new Product("Atomic Habits", 499);
        CartItem phone   = new Product("iPhone 15", 79999);
        CartItem earbuds = new Product("AirPods", 15999);
        CartItem charger = new Product("20W Charger", 1999);

        // A bundle of leaves
        ProductBundle iphoneCombo = new ProductBundle("iPhone Essentials Combo");
        iphoneCombo.addItem(phone);
        iphoneCombo.addItem(earbuds);
        iphoneCombo.addItem(charger);

        ProductBundle schoolKit = new ProductBundle("Back to School Kit");
        schoolKit.addItem(new Product("Notebook Pack", 249));
        schoolKit.addItem(new Product("Pen Set", 99));

        // RECURSION: a bundle that CONTAINS another bundle (composite inside composite)
        ProductBundle festiveMegaPack = new ProductBundle("Festive Mega Pack");
        festiveMegaPack.addItem(iphoneCombo);   // a bundle...
        festiveMegaPack.addItem(book);          // ...next to a single product
        festiveMegaPack.addItem(schoolKit);     // ...next to another bundle

        // The cart is a List<CartItem> — no List<Object>, no instanceof
        List<CartItem> cart = new ArrayList<>();
        cart.add(book);
        cart.add(iphoneCombo);
        cart.add(festiveMegaPack);   // nested tree handled the same as a single book

        System.out.println("Your Amazon Cart:");
        double total = 0;
        for (CartItem item : cart) {  // one loop treats leaf and tree identically
            item.display("  ");
            total += item.getPrice();
        }

        System.out.println("\nTotal: Rs " + total);
    }
}
