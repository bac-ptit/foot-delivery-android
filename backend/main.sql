-- ============================================================
-- PostgreSQL Schema - Generated from Visual Paradigm project.xml
-- ============================================================
-- Note: "Order" is a reserved keyword in PostgreSQL, renamed to "orders"
-- ============================================================

CREATE TABLE Category (
                          ID SERIAL PRIMARY KEY,
                          name VARCHAR(255) NOT NULL,
                          type VARCHAR(255),
                          description TEXT
);

CREATE TABLE FAQ (
                     ID SERIAL PRIMARY KEY,
                     question VARCHAR(255),
                     answer TEXT,
                     isactive BOOLEAN DEFAULT TRUE
);

CREATE TABLE "User" (
                        ID SERIAL PRIMARY KEY,
                        name VARCHAR(255) NOT NULL,
                        username VARCHAR(255) NOT NULL,
                        email VARCHAR(255) NOT NULL,
                        password VARCHAR(255) NOT NULL,
                        phone VARCHAR(255) NOT NULL,
                        role VARCHAR(255) NOT NULL
);

CREATE TABLE Shipper (
                         ID SERIAL PRIMARY KEY,
                         name VARCHAR(255) NOT NULL,
                         phone VARCHAR(255) NOT NULL,
                         rating INTEGER,
                         description TEXT,
                         status VARCHAR(255) NOT NULL
);

CREATE TABLE Promotion (
                           ID SERIAL PRIMARY KEY,
                           code VARCHAR(255),
                           discounttype VARCHAR(255),
                           discountvalue INTEGER,
                           expiredate DATE,
                           minordervalue INTEGER,
                           status VARCHAR(255) NOT NULL
);

CREATE TABLE Restaurant (
                            ID SERIAL PRIMARY KEY,
                            name VARCHAR(255) NOT NULL,
                            image_url TEXT,
                            address VARCHAR(255),
                            rating INTEGER,
                            open_time TIME,
                            close_time TIME,
                            phone_number VARCHAR(255) NOT NULL,
                            status VARCHAR(255) NOT NULL,
                            description TEXT
);

CREATE TABLE MenuItem (
                          ID SERIAL PRIMARY KEY,
                          name VARCHAR(255) NOT NULL,
                          image_url TEXT,
                          price INTEGER,
                          is_available BOOLEAN DEFAULT TRUE,
                          description TEXT,
                          restaurantid INTEGER,
                          categoryid INTEGER,
                          CONSTRAINT fk_menuitem_restaurantid FOREIGN KEY (restaurantid) REFERENCES Restaurant(ID),
                          CONSTRAINT fk_menuitem_categoryid FOREIGN KEY (categoryid) REFERENCES Category(ID)
);

CREATE TABLE Address (
                         ID SERIAL PRIMARY KEY,
                         detail TEXT,
                         phone VARCHAR(255),
                         userid INTEGER,
                         CONSTRAINT fk_address_userid FOREIGN KEY (userid) REFERENCES "User"(ID)
);

CREATE TABLE orders (
                        ID SERIAL PRIMARY KEY,
                        status VARCHAR(255) NOT NULL,
                        createdat TIMESTAMP NOT NULL DEFAULT NOW(),
                        preorderdate DATE,
                        preordertime TIME,
                        totalprice INTEGER,
                        restaurantid INTEGER,
                        addressid INTEGER,
                        userid INTEGER,
                        CONSTRAINT fk_orders_restaurantid FOREIGN KEY (restaurantid) REFERENCES Restaurant(ID),
                        CONSTRAINT fk_orders_addressid FOREIGN KEY (addressid) REFERENCES Address(ID),
                        CONSTRAINT fk_orders_userid FOREIGN KEY (userid) REFERENCES "User"(ID)
);

CREATE TABLE orderitem (
                           ID SERIAL PRIMARY KEY,
                           quantity INTEGER NOT NULL,
                           price INTEGER NOT NULL,
                           menuitemid INTEGER,
                           orderid INTEGER,
                           CONSTRAINT fk_orderitem_menuitemid FOREIGN KEY (menuitemid) REFERENCES MenuItem(ID),
                           CONSTRAINT fk_orderitem_orderid FOREIGN KEY (orderid) REFERENCES orders(ID)
);

CREATE TABLE payment (
                         ID SERIAL PRIMARY KEY,
                         status VARCHAR(255) NOT NULL,
                         method VARCHAR(255),
                         orderid INTEGER,
                         CONSTRAINT fk_payment_orderid FOREIGN KEY (orderid) REFERENCES orders(ID)
);

CREATE TABLE delivery (
                          ID SERIAL PRIMARY KEY,
                          status VARCHAR(255) NOT NULL,
                          deliverytime TIME,
                          orderid INTEGER,
                          shipperid INTEGER,
                          CONSTRAINT fk_delivery_orderid FOREIGN KEY (orderid) REFERENCES orders(ID),
                          CONSTRAINT fk_delivery_shipperid FOREIGN KEY (shipperid) REFERENCES Shipper(ID)
);

CREATE TABLE review (
                        ID SERIAL PRIMARY KEY,
                        rating INTEGER,
                        comment TEXT,
                        menuitemid INTEGER,
                        restaurantid INTEGER,
                        userid INTEGER,
                        orderid INTEGER,
                        CONSTRAINT fk_review_menuitemid FOREIGN KEY (menuitemid) REFERENCES MenuItem(ID),
                        CONSTRAINT fk_review_restaurantid FOREIGN KEY (restaurantid) REFERENCES Restaurant(ID),
                        CONSTRAINT fk_review_userid FOREIGN KEY (userid) REFERENCES "User"(ID),
                        CONSTRAINT fk_review_orderid FOREIGN KEY (orderid) REFERENCES orders(ID)
);

CREATE TABLE chatsession (
                             ID SERIAL PRIMARY KEY,
                             createdat TIMESTAMP NOT NULL DEFAULT NOW(),
                             status VARCHAR(255) NOT NULL,
                             userid INTEGER,
                             CONSTRAINT fk_chatsession_userid FOREIGN KEY (userid) REFERENCES "User"(ID)
);

CREATE TABLE chatmessage (
                             ID SERIAL PRIMARY KEY,
                             senderrole VARCHAR(255),
                             message TEXT,
                             sentat TIMESTAMP DEFAULT NOW(),
                             sessionid INTEGER,
                             CONSTRAINT fk_chatmessage_sessionid FOREIGN KEY (sessionid) REFERENCES ChatSession(ID)
);

CREATE TABLE notification (
                              ID SERIAL PRIMARY KEY,
                              title VARCHAR(255),
                              type VARCHAR(255),
                              content TEXT,
                              isread BOOLEAN DEFAULT FALSE,
                              createdat TIMESTAMP NOT NULL DEFAULT NOW(),
                              userid INTEGER,
                              orderid INTEGER,
                              sessionid INTEGER,
                              CONSTRAINT fk_notification_userid FOREIGN KEY (userid) REFERENCES "User"(ID),
                              CONSTRAINT fk_notification_orderid FOREIGN KEY (orderid) REFERENCES orders(ID),
                              CONSTRAINT fk_notification_sessionid FOREIGN KEY (sessionid) REFERENCES ChatSession(ID)
);

CREATE TABLE usedpromotion (
                               ID SERIAL PRIMARY KEY,
                               usedat DATE,
                               promotionid INTEGER,
                               orderid INTEGER,
                               CONSTRAINT fk_usedpromotion_promotionid FOREIGN KEY (promotionid) REFERENCES Promotion(ID),
                               CONSTRAINT fk_usedpromotion_orderid FOREIGN KEY (orderid) REFERENCES orders(ID)
);

CREATE TABLE loyaltypoint (
                              ID SERIAL PRIMARY KEY,
                              points INTEGER DEFAULT 0,
                              updatedat DATE,
                              userid INTEGER,
                              menuitemid INTEGER,
                              CONSTRAINT fk_loyaltypoint_userid FOREIGN KEY (userid) REFERENCES "User"(ID),
                              CONSTRAINT fk_loyaltypoint_menuitemid FOREIGN KEY (menuitemid) REFERENCES MenuItem(ID)
);

CREATE TABLE socialshare (
                             ID SERIAL PRIMARY KEY,
                             sharetype VARCHAR(255),
                             platform VARCHAR(255),
                             context TEXT,
                             createdat DATE NOT NULL,
                             userid INTEGER,
                             restaurantid INTEGER,
                             menuitemid INTEGER,
                             CONSTRAINT fk_socialshare_userid FOREIGN KEY (userid) REFERENCES "User"(ID),
                             CONSTRAINT fk_socialshare_restaurantid FOREIGN KEY (restaurantid) REFERENCES Restaurant(ID),
                             CONSTRAINT fk_socialshare_menuitemid FOREIGN KEY (menuitemid) REFERENCES MenuItem(ID)
);

CREATE TABLE userfaq (
                         ID SERIAL PRIMARY KEY,
                         viewedat TIMESTAMP DEFAULT NOW(),
                         userid INTEGER,
                         faqid INTEGER,
                         CONSTRAINT fk_userfaq_userid FOREIGN KEY (userid) REFERENCES "User"(ID),
                         CONSTRAINT fk_userfaq_faqid FOREIGN KEY (faqid) REFERENCES FAQ(ID)
);

CREATE TABLE userdevice (
                            ID SERIAL PRIMARY KEY,
                            device_token VARCHAR(255) NOT NULL,
                            device_type VARCHAR(255),
                            last_active TIMESTAMP DEFAULT NOW(),
                            userid INTEGER,
                            CONSTRAINT fk_userdevice_userid FOREIGN KEY (userid) REFERENCES "User"(ID)
);
