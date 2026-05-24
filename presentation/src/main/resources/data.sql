-- ===================================================
-- テストユーザー
-- パスワード: password123 (BCrypt ハッシュ)
-- ===================================================
INSERT INTO users (id, email, password, name, company_name, role, created_at, updated_at)
VALUES ('10000000-0000-0000-0000-000000000001',
        'test@example.com',
        '$2a$10$NAHEExCWcgPAOiXkeB1iRePTJQd1ZdcLCUq7.HJDd0IuU195sXzHe',
        '山田 太郎',
        'テスト株式会社',
        'BUYER',
        '2024-01-01 00:00:00',
        '2024-01-01 00:00:00');

INSERT INTO users (id, email, password, name, company_name, role, created_at, updated_at)
VALUES ('10000000-0000-0000-0000-000000000002',
        'admin@example.com',
        '$2a$10$NAHEExCWcgPAOiXkeB1iRePTJQd1ZdcLCUq7.HJDd0IuU195sXzHe',
        '管理者 花子',
        'サンプルEC運営',
        'ADMIN',
        '2024-01-01 00:00:00',
        '2024-01-01 00:00:00');

-- ===================================================
-- テスト商品 (8件: furniture×2, accessories×2, electronics×2, lighting×2)
-- ===================================================

-- furniture
INSERT INTO products (id, name, description, price, currency, stock, category, image_url, version, created_at, updated_at)
VALUES ('20000000-0000-0000-0000-000000000001',
        'ウォールナット ダイニングテーブル',
        '天然ウォールナット材を使用した上品なダイニングテーブル。4〜6人でゆったり使える150cmサイズ。',
        89000.00, 'JPY', 5, 'furniture',
        'https://images.unsplash.com/photo-1555041469-a586c61ea9bc?w=600',
        0, '2024-01-10 00:00:00', '2024-01-10 00:00:00');

INSERT INTO products (id, name, description, price, currency, stock, category, image_url, version, created_at, updated_at)
VALUES ('20000000-0000-0000-0000-000000000002',
        'スカンジナビアン ソファ 3人掛け',
        'シンプルで洗練されたスカンジナビアンデザインの3人掛けソファ。高品質なファブリック素材使用。',
        128000.00, 'JPY', 3, 'furniture',
        'https://images.unsplash.com/photo-1555041469-a586c61ea9bc?w=600',
        0, '2024-01-11 00:00:00', '2024-01-11 00:00:00');

-- accessories
INSERT INTO products (id, name, description, price, currency, stock, category, image_url, version, created_at, updated_at)
VALUES ('20000000-0000-0000-0000-000000000003',
        'ハンドクラフト セラミックマグ',
        '職人が一点一点手作りしたセラミックマグ。温かみのある風合いと持ちやすいフォルム。',
        3800.00, 'JPY', 20, 'accessories',
        'https://images.unsplash.com/photo-1514228742587-6b1558fcca3d?w=600',
        0, '2024-01-12 00:00:00', '2024-01-12 00:00:00');

INSERT INTO products (id, name, description, price, currency, stock, category, image_url, version, created_at, updated_at)
VALUES ('20000000-0000-0000-0000-000000000004',
        'レザー トートバッグ',
        '本革を使用したシンプルなトートバッグ。A4書類も入る広めの収納スペース。使うほど味が出るイタリアンレザー。',
        24500.00, 'JPY', 8, 'accessories',
        'https://images.unsplash.com/photo-1548036328-c9fa89d128fa?w=600',
        0, '2024-01-13 00:00:00', '2024-01-13 00:00:00');

-- electronics
INSERT INTO products (id, name, description, price, currency, stock, category, image_url, version, created_at, updated_at)
VALUES ('20000000-0000-0000-0000-000000000005',
        'ワイヤレス ノイズキャンセリングヘッドフォン',
        'アクティブノイズキャンセリング機能搭載の高音質ヘッドフォン。最大30時間の連続再生が可能。',
        32000.00, 'JPY', 12, 'electronics',
        'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=600',
        0, '2024-01-14 00:00:00', '2024-01-14 00:00:00');

INSERT INTO products (id, name, description, price, currency, stock, category, image_url, version, created_at, updated_at)
VALUES ('20000000-0000-0000-0000-000000000006',
        '4K ウェブカメラ',
        'テレワークやストリーミングに最適な4K対応ウェブカメラ。オートフォーカス・内蔵マイク付き。',
        15800.00, 'JPY', 0, 'electronics',
        'https://images.unsplash.com/photo-1587826080692-f439cd0b70a1?w=600',
        0, '2024-01-15 00:00:00', '2024-01-15 00:00:00');

-- lighting
INSERT INTO products (id, name, description, price, currency, stock, category, image_url, version, created_at, updated_at)
VALUES ('20000000-0000-0000-0000-000000000007',
        'ペンダント LED ライト',
        '真鍮製ペンダントライト。温白色 LED 採用で落ち着いた雰囲気を演出。ダイニングやキッチンに最適。',
        18500.00, 'JPY', 7, 'lighting',
        'https://images.unsplash.com/photo-1524484485831-a92ffc0de03f?w=600',
        0, '2024-01-16 00:00:00', '2024-01-16 00:00:00');

INSERT INTO products (id, name, description, price, currency, stock, category, image_url, version, created_at, updated_at)
VALUES ('20000000-0000-0000-0000-000000000008',
        'デスク フロアランプ',
        'アーム可動式のフロアランプ。読書や作業に最適な調光機能付き。スタイリッシュなマット仕上げ。',
        12800.00, 'JPY', 10, 'lighting',
        'https://images.unsplash.com/photo-1507473885765-e6ed057f782c?w=600',
        0, '2024-01-17 00:00:00', '2024-01-17 00:00:00');

-- ===================================================
-- テスト注文
-- ===================================================

-- 注文1: delivered (buyer が family furniture を注文)
INSERT INTO orders (id, buyer_id, total_price, status, created_at, updated_at)
VALUES ('30000000-0000-0000-0000-000000000001',
        '10000000-0000-0000-0000-000000000001',
        92800.00, 'DELIVERED',
        '2024-02-01 10:00:00', '2024-02-10 15:00:00');

INSERT INTO order_items (id, order_id, product_id, quantity, unit_price)
VALUES ('40000000-0000-0000-0000-000000000001',
        '30000000-0000-0000-0000-000000000001',
        '20000000-0000-0000-0000-000000000001',
        1, 89000.00);

INSERT INTO order_items (id, order_id, product_id, quantity, unit_price)
VALUES ('40000000-0000-0000-0000-000000000002',
        '30000000-0000-0000-0000-000000000001',
        '20000000-0000-0000-0000-000000000003',
        1, 3800.00);

-- 注文2: confirmed (buyer がヘッドフォンを注文)
INSERT INTO orders (id, buyer_id, total_price, status, created_at, updated_at)
VALUES ('30000000-0000-0000-0000-000000000002',
        '10000000-0000-0000-0000-000000000001',
        64000.00, 'CONFIRMED',
        '2024-03-15 09:00:00', '2024-03-15 09:05:00');

INSERT INTO order_items (id, order_id, product_id, quantity, unit_price)
VALUES ('40000000-0000-0000-0000-000000000003',
        '30000000-0000-0000-0000-000000000002',
        '20000000-0000-0000-0000-000000000005',
        2, 32000.00);
