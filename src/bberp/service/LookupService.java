package bberp.service;

import com.zaxxer.hikari.HikariDataSource;
import bberp.dao.CustomerDao;
import bberp.dao.ProductDao;
import bberp.model.CustomerModel;
import bberp.model.ProductModel;
import java.util.List;

public class LookupService {
    private final CustomerDao customerDao;
    private final ProductDao productDao;

    public LookupService(HikariDataSource ds) {
        this.customerDao = new CustomerDao(ds);
        this.productDao = new ProductDao(ds);
    }

    public List<CustomerModel> customers() { return customerDao.findAll(); }
    public List<ProductModel> products()   { return productDao.findAll(); }
}
