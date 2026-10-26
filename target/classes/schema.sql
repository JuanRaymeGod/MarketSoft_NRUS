CREATE TABLE IF NOT EXISTS categorias (
    id_categoria INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS productos (
    id_producto INTEGER PRIMARY KEY AUTOINCREMENT,
    codigo_barras TEXT UNIQUE,
    nombre TEXT NOT NULL,
    id_categoria INTEGER,
    precio_compra_unitario REAL NOT NULL DEFAULT 0.0,
    precio_venta_unitario REAL NOT NULL DEFAULT 0.0,
    viene_en_caja INTEGER DEFAULT 0,
    unidades_por_caja INTEGER DEFAULT 1,
    precio_compra_caja REAL DEFAULT 0.0,
    stock_unidades INTEGER NOT NULL DEFAULT 0,
    stock_minimo INTEGER DEFAULT 5,
    fecha_vencimiento DATE,
    activo INTEGER NOT NULL DEFAULT 1,
    FOREIGN KEY (id_categoria) REFERENCES categorias(id_categoria)
);

CREATE TABLE IF NOT EXISTS ventas (
    id_venta INTEGER PRIMARY KEY AUTOINCREMENT,
    fecha_hora DATETIME DEFAULT CURRENT_TIMESTAMP,
    total REAL NOT NULL,
    monto_recibido REAL NOT NULL,
    vuelto REAL NOT NULL,
    metodo_pago TEXT CHECK(metodo_pago IN ('Efectivo', 'Yape', 'Plin')) DEFAULT 'Efectivo',
    dni_cliente TEXT NULL,
    nombre_cliente TEXT NULL
);

CREATE TABLE IF NOT EXISTS detalle_ventas (
    id_detalle INTEGER PRIMARY KEY AUTOINCREMENT,
    id_venta INTEGER NOT NULL,
    id_producto INTEGER NOT NULL,
    cantidad INTEGER NOT NULL,
    precio_unitario REAL NOT NULL,
    costo_unitario_compra REAL,
    subtotal REAL NOT NULL,
    FOREIGN KEY (id_venta) REFERENCES ventas(id_venta) ON DELETE CASCADE,
    FOREIGN KEY (id_producto) REFERENCES productos(id_producto)
);

CREATE TABLE IF NOT EXISTS usuarios (
    id_usuario INTEGER PRIMARY KEY AUTOINCREMENT,
    usuario TEXT NOT NULL UNIQUE,
    contrasena TEXT NOT NULL,
    rol TEXT CHECK(rol IN ('Administrador','Cajero')) NOT NULL
);

CREATE TABLE IF NOT EXISTS mermas (
    id_merma INTEGER PRIMARY KEY AUTOINCREMENT,
    id_producto INTEGER NOT NULL,
    cantidad INTEGER NOT NULL,
    motivo TEXT,
    fecha DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_producto) REFERENCES productos(id_producto)
);