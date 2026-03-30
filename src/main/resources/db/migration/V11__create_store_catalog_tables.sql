-- V11: Tienda / Catálogo de productos de la academia

-- ================================================
-- 1. Categorías de productos
-- ================================================
CREATE TABLE IF NOT EXISTS product_categories (
    id          BIGSERIAL       PRIMARY KEY,
    name        VARCHAR(100)    NOT NULL UNIQUE,
    slug        VARCHAR(100)    NOT NULL UNIQUE,
    description TEXT,
    image_url   VARCHAR(500),
    is_active   BOOLEAN         NOT NULL DEFAULT TRUE,
    sort_order  INT             NOT NULL DEFAULT 0,
    created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
);

-- ================================================
-- 2. Productos
-- ================================================
CREATE TABLE IF NOT EXISTS products (
    id              BIGSERIAL       PRIMARY KEY,
    category_id     BIGINT          NOT NULL,
    name            VARCHAR(200)    NOT NULL,
    slug            VARCHAR(200)    NOT NULL UNIQUE,
    description     TEXT,
    base_price      DECIMAL(10,2)   NOT NULL,
    image_url       VARCHAR(500),
    image_urls      TEXT[],          -- arreglo de URLs adicionales
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    featured        BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES product_categories(id) ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_products_category    ON products(category_id);
CREATE INDEX IF NOT EXISTS idx_products_is_active   ON products(is_active);
CREATE INDEX IF NOT EXISTS idx_products_featured    ON products(featured);

-- ================================================
-- 3. Variantes de producto (talla/color + stock)
-- ================================================
CREATE TABLE IF NOT EXISTS product_variants (
    id              BIGSERIAL       PRIMARY KEY,
    product_id      BIGINT          NOT NULL,
    size            VARCHAR(10)     NOT NULL
        CHECK (size IN ('XS','S','M','L','XL','XXL','UNICO')),
    color           VARCHAR(50),
    sku             VARCHAR(100)    UNIQUE,
    stock           INT             NOT NULL DEFAULT 0,
    additional_price DECIMAL(10,2)  NOT NULL DEFAULT 0.00,
    is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_variants_product  FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT chk_stock_positive   CHECK (stock >= 0)
);

CREATE INDEX IF NOT EXISTS idx_variants_product ON product_variants(product_id);

-- ================================================
-- 4. Carritos de compra
-- ================================================
CREATE TABLE IF NOT EXISTS shopping_carts (
    id          BIGSERIAL   PRIMARY KEY,
    user_id     BIGINT      UNIQUE,   -- NULL = carrito anónimo (sesión)
    session_key VARCHAR(128),         -- Para carritos anónimos
    created_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_cart_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_carts_user    ON shopping_carts(user_id);
CREATE INDEX IF NOT EXISTS idx_carts_session ON shopping_carts(session_key);

-- ================================================
-- 5. Ítems del carrito
-- ================================================
CREATE TABLE IF NOT EXISTS cart_items (
    id          BIGSERIAL   PRIMARY KEY,
    cart_id     BIGINT      NOT NULL,
    variant_id  BIGINT      NOT NULL,
    quantity    INT         NOT NULL DEFAULT 1,
    added_at    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_cart_items_cart    FOREIGN KEY (cart_id)   REFERENCES shopping_carts(id) ON DELETE CASCADE,
    CONSTRAINT fk_cart_items_variant FOREIGN KEY (variant_id) REFERENCES product_variants(id) ON DELETE CASCADE,
    CONSTRAINT chk_cart_quantity     CHECK (quantity > 0),
    CONSTRAINT uq_cart_variant       UNIQUE (cart_id, variant_id)
);

CREATE INDEX IF NOT EXISTS idx_cart_items_cart ON cart_items(cart_id);

-- ================================================
-- 6. Órdenes de la tienda
-- ================================================
CREATE TABLE IF NOT EXISTS store_orders (
    id              BIGSERIAL       PRIMARY KEY,
    order_number    VARCHAR(30)     NOT NULL UNIQUE,
    user_id         BIGINT,
    customer_name   VARCHAR(200)    NOT NULL,
    customer_email  VARCHAR(255)    NOT NULL,
    customer_phone  VARCHAR(30),
    status          VARCHAR(30)     NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('PENDING','CONFIRMED','PAID','PREPARING','SHIPPED','DELIVERED','CANCELLED','REFUNDED')),
    payment_method  VARCHAR(30)
        CHECK (payment_method IN ('CASH','CREDIT_CARD','DEBIT_CARD','BANK_TRANSFER','WEBPAY','MERCADOPAGO')),
    subtotal        DECIMAL(12,2)   NOT NULL,
    discount_amount DECIMAL(12,2)   NOT NULL DEFAULT 0.00,
    total_amount    DECIMAL(12,2)   NOT NULL,
    notes           TEXT,
    shipping_address TEXT,
    paid_at         TIMESTAMP,
    shipped_at      TIMESTAMP,
    delivered_at    TIMESTAMP,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_store_orders_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_store_orders_user   ON store_orders(user_id);
CREATE INDEX IF NOT EXISTS idx_store_orders_status ON store_orders(status);
CREATE INDEX IF NOT EXISTS idx_store_orders_number ON store_orders(order_number);

-- ================================================
-- 7. Ítems de la orden
-- ================================================
CREATE TABLE IF NOT EXISTS store_order_items (
    id              BIGSERIAL       PRIMARY KEY,
    order_id        BIGINT          NOT NULL,
    variant_id      BIGINT,
    product_name    VARCHAR(200)    NOT NULL,
    variant_size    VARCHAR(10),
    variant_color   VARCHAR(50),
    quantity        INT             NOT NULL,
    unit_price      DECIMAL(10,2)   NOT NULL,
    subtotal        DECIMAL(12,2)   NOT NULL,

    CONSTRAINT fk_order_items_order   FOREIGN KEY (order_id)   REFERENCES store_orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_order_items_variant FOREIGN KEY (variant_id) REFERENCES product_variants(id) ON DELETE SET NULL,
    CONSTRAINT chk_order_item_qty     CHECK (quantity > 0)
);

CREATE INDEX IF NOT EXISTS idx_order_items_order ON store_order_items(order_id);

-- ================================================
-- 8. Datos semilla — categorías y productos demo
-- ================================================
INSERT INTO product_categories (name, slug, description, sort_order) VALUES
    ('Poleras',    'poleras',    'Poleras con el diseño oficial de la academia',    1),
    ('Polerones',  'polerones',  'Polerones y sudaderas de la academia de baile',   2),
    ('Tazas',      'tazas',      'Tazas con el logo de Estilo D'' Mua',             3),
    ('Accesorios', 'accesorios', 'Accesorios varios de la academia',                4)
ON CONFLICT (slug) DO NOTHING;

-- Productos demo (imagen placeholder, precios en CLP)
INSERT INTO products (category_id, name, slug, description, base_price, featured) VALUES
    (
        (SELECT id FROM product_categories WHERE slug = 'poleras'),
        'Polera Estilo D'' Mua Classic',
        'polera-classic',
        'Polera de algodón 100% con el logo bordado de la academia. Disponible en varios colores.',
        14990,
        TRUE
    ),
    (
        (SELECT id FROM product_categories WHERE slug = 'poleras'),
        'Polera Estilo D'' Mua Dancer',
        'polera-dancer',
        'Polera liviana de poliéster ideal para bailar, con diseño exclusivo de la academia.',
        12990,
        FALSE
    ),
    (
        (SELECT id FROM product_categories WHERE slug = 'polerones'),
        'Polerón Estilo D'' Mua Premium',
        'poleron-premium',
        'Polerón de algodón rizo con capucha, logotipo de la academia en serigrafía.',
        24990,
        TRUE
    ),
    (
        (SELECT id FROM product_categories WHERE slug = 'polerones'),
        'Polerón Estilo D'' Mua Zip',
        'poleron-zip',
        'Polerón con cierre completo y logo en el pecho. Ideal para el frío del estudio.',
        27990,
        FALSE
    ),
    (
        (SELECT id FROM product_categories WHERE slug = 'tazas'),
        'Taza Estilo D'' Mua',
        'taza-classic',
        'Taza de 350ml con el logo de la academia, perfecta para tu café antes de clases.',
        8990,
        TRUE
    ),
    (
        (SELECT id FROM product_categories WHERE slug = 'tazas'),
        'Taza Mágica Estilo D'' Mua',
        'taza-magica',
        'Taza termosensible que revela el logo al agregar líquido caliente.',
        11990,
        FALSE
    )
ON CONFLICT (slug) DO NOTHING;

-- Variantes para poleras
INSERT INTO product_variants (product_id, size, color, sku, stock) VALUES
    ((SELECT id FROM products WHERE slug = 'polera-classic'), 'S',   'Negro',  'POL-CLA-S-NEG', 10),
    ((SELECT id FROM products WHERE slug = 'polera-classic'), 'M',   'Negro',  'POL-CLA-M-NEG', 15),
    ((SELECT id FROM products WHERE slug = 'polera-classic'), 'L',   'Negro',  'POL-CLA-L-NEG', 12),
    ((SELECT id FROM products WHERE slug = 'polera-classic'), 'XL',  'Negro',  'POL-CLA-XL-NEG', 8),
    ((SELECT id FROM products WHERE slug = 'polera-classic'), 'S',   'Blanco', 'POL-CLA-S-BLA',  10),
    ((SELECT id FROM products WHERE slug = 'polera-classic'), 'M',   'Blanco', 'POL-CLA-M-BLA',  15),
    ((SELECT id FROM products WHERE slug = 'polera-classic'), 'L',   'Blanco', 'POL-CLA-L-BLA',  10),
    ((SELECT id FROM products WHERE slug = 'polera-dancer'),  'S',   'Negro',  'POL-DAN-S-NEG',  8),
    ((SELECT id FROM products WHERE slug = 'polera-dancer'),  'M',   'Negro',  'POL-DAN-M-NEG',  12),
    ((SELECT id FROM products WHERE slug = 'polera-dancer'),  'L',   'Negro',  'POL-DAN-L-NEG',  10),
    ((SELECT id FROM products WHERE slug = 'poleron-premium'),'S',   'Negro',  'POL-PRE-S-NEG',  6),
    ((SELECT id FROM products WHERE slug = 'poleron-premium'),'M',   'Negro',  'POL-PRE-M-NEG',  10),
    ((SELECT id FROM products WHERE slug = 'poleron-premium'),'L',   'Negro',  'POL-PRE-L-NEG',  8),
    ((SELECT id FROM products WHERE slug = 'poleron-premium'),'XL',  'Negro',  'POL-PRE-XL-NEG', 5),
    ((SELECT id FROM products WHERE slug = 'poleron-zip'),    'M',   'Gris',   'POL-ZIP-M-GRI',  6),
    ((SELECT id FROM products WHERE slug = 'poleron-zip'),    'L',   'Gris',   'POL-ZIP-L-GRI',  8),
    ((SELECT id FROM products WHERE slug = 'poleron-zip'),    'XL',  'Gris',   'POL-ZIP-XL-GRI', 4),
    ((SELECT id FROM products WHERE slug = 'taza-classic'),   'UNICO','Blanco','TAZ-CLA-UNICO', 30),
    ((SELECT id FROM products WHERE slug = 'taza-magica'),    'UNICO','Blanco','TAZ-MAG-UNICO', 20)
ON CONFLICT (sku) DO NOTHING;
