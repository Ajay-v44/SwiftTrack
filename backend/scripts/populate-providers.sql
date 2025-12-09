-- Script to populate provider data in SwiftTrack database
-- Based on research from Firecrawl

-- Insert providers
INSERT INTO provider (
    id, 
    provider_name, 
    provider_code, 
    description, 
    logo_url, 
    website_url, 
    supports_hyperlocal, 
    supports_courier, 
    supports_same_day, 
    supports_intercity, 
    config_schema, 
    is_active, 
    allow_self_registration, 
    created_by_id, 
    updated_by,
    created_at,
    updated_at
) VALUES 
-- Shadowfax
('11111111-1111-1111-1111-111111111111', 
 'Shadowfax', 
 'SHADOWFAX', 
 'Leading logistics service provider specializing in hyperlocal delivery, last-mile logistics, and crowdsourced delivery model. Serving over 14,700 PIN codes with more than 3,000 trucks.',
 'https://www.shadowfax.in/logo_header_tm.svg', 
 'https://www.shadowfax.in/', 
 true,  -- supports_hyperlocal
 true,  -- supports_courier
 true,  -- supports_same_day
 true,  -- supports_intercity
 '{}', 
 true, 
 true, 
 '00000000-0000-0000-0000-000000000000', 
 '00000000-0000-0000-0000-000000000000',
 NOW(),
 NOW()),

-- Porter
('22222222-2222-2222-2222-222222222222', 
 'Porter', 
 'PORTER', 
 'Leading player in tech-enabled intra-city logistics. Present in 20+ cities of India and 2 International locations (UAE & Bangladesh).',
 '', 
 'https://porter.in/', 
 true,  -- supports_hyperlocal
 true,  -- supports_courier
 false, -- supports_same_day
 true,  -- supports_intercity
 '{}', 
 true, 
 true, 
 '00000000-0000-0000-0000-000000000000', 
 '00000000-0000-0000-0000-000000000000',
 NOW(),
 NOW()),

-- Dunzo
('33333333-3333-3333-3333-333333333333', 
 'Dunzo', 
 'DUNZO', 
 'Indian on-demand delivery company offering services in major cities like Delhi, Bengaluru, Gurugram, Chennai, Pune, Mumbai, Hyderabad, and Jaipur.',
 '', 
 'https://www.dunzo.com/', 
 true,  -- supports_hyperlocal
 true,  -- supports_courier
 true,  -- supports_same_day
 false, -- supports_intercity
 '{}', 
 true, 
 true, 
 '00000000-0000-0000-0000-000000000000', 
 '00000000-0000-0000-0000-000000000000',
 NOW(),
 NOW()),

-- Rapido
('44444444-4444-4444-4444-444444444444', 
 'Rapido', 
 'RAPIDO', 
 'Indian ride-hailing service, which primarily operates as a bike taxi aggregator. Its offerings also include auto rickshaw and taxicab hailing. Works in more than 90+ urban areas the nation over.',
 '', 
 'https://www.rapido.bike/', 
 true,  -- supports_hyperlocal
 true,  -- supports_courier
 false, -- supports_same_day
 true,  -- supports_intercity
 '{}', 
 true, 
 true, 
 '00000000-0000-0000-0000-000000000000', 
 '00000000-0000-0000-0000-000000000000',
 NOW(),
 NOW()),

-- Borzo (formerly WeFast)
('55555555-5555-5555-5555-555555555555', 
 'Borzo', 
 'BORZO', 
 'Fast & affordable courier service operating in major Indian cities like Mumbai, Delhi, Bangalore, Hyderabad, Chennai, Pune, and more.',
 '', 
 'https://borzodelivery.com/in/', 
 true,  -- supports_hyperlocal
 true,  -- supports_courier
 true,  -- supports_same_day
 false, -- supports_intercity
 '{}', 
 true, 
 true, 
 '00000000-0000-0000-0000-000000000000', 
 '00000000-0000-0000-0000-000000000000',
 NOW(),
 NOW()),

-- MOVER
('66666666-6666-6666-6666-666666666666', 
 'MOVER', 
 'MOVER', 
 'On-demand hyperlocal bike and truck booking app operating across all major cities in India. Guarantees timely and safe goods delivery through its efficient online delivery app.',
 '', 
 'https://mover.delivery/', 
 true,  -- supports_hyperlocal
 true,  -- supports_courier
 false, -- supports_same_day
 true,  -- supports_intercity
 '{}', 
 true, 
 true, 
 '00000000-0000-0000-0000-000000000000', 
 '00000000-0000-0000-0000-000000000000',
 NOW(),
 NOW());

-- Insert serviceable areas for Shadowfax
INSERT INTO provider_servicable_areas (
    id,
    provider_id,
    state,
    city,
    pin_code,
    is_active,
    created_by,
    updated_by,
    created_at,
    updated_at
) VALUES
-- Shadowfax coverage
('11111111-1111-1111-1111-111111111112',
 '11111111-1111-1111-1111-111111111111',
 'Karnataka',
 'Bangalore',
 '',
 true,
 '00000000-0000-0000-0000-000000000000',
 '00000000-0000-0000-0000-000000000000',
 NOW(),
 NOW()),

('11111111-1111-1111-1111-111111111113',
 '11111111-1111-1111-1111-111111111111',
 'Delhi',
 'Delhi',
 '',
 true,
 '00000000-0000-0000-0000-000000000000',
 '00000000-0000-0000-0000-000000000000',
 NOW(),
 NOW()),

('11111111-1111-1111-1111-111111111114',
 '11111111-1111-1111-1111-111111111111',
 'Maharashtra',
 'Mumbai',
 '',
 true,
 '00000000-0000-0000-0000-000000000000',
 '00000000-0000-0000-0000-000000000000',
 NOW(),
 NOW()),

-- Porter coverage
('22222222-2222-2222-2222-222222222223',
 '22222222-2222-2222-2222-222222222222',
 'Karnataka',
 'Bangalore',
 '',
 true,
 '00000000-0000-0000-0000-000000000000',
 '00000000-0000-0000-0000-000000000000',
 NOW(),
 NOW()),

('22222222-2222-2222-2222-222222222224',
 '22222222-2222-2222-2222-222222222222',
 'Delhi',
 'Delhi',
 '',
 true,
 '00000000-0000-0000-0000-000000000000',
 '00000000-0000-0000-0000-000000000000',
 NOW(),
 NOW()),

('22222222-2222-2222-2222-222222222225',
 '22222222-2222-2222-2222-222222222222',
 'Maharashtra',
 'Mumbai',
 '',
 true,
 '00000000-0000-0000-0000-000000000000',
 '00000000-0000-0000-0000-000000000000',
 NOW(),
 NOW()),

-- Dunzo coverage
('33333333-3333-3333-3333-333333333334',
 '33333333-3333-3333-3333-333333333333',
 'Karnataka',
 'Bangalore',
 '',
 true,
 '00000000-0000-0000-0000-000000000000',
 '00000000-0000-0000-0000-000000000000',
 NOW(),
 NOW()),

('33333333-3333-3333-3333-333333333335',
 '33333333-3333-3333-3333-333333333333',
 'Delhi',
 'Delhi',
 '',
 true,
 '00000000-0000-0000-0000-000000000000',
 '00000000-0000-0000-0000-000000000000',
 NOW(),
 NOW()),

('33333333-3333-3333-3333-333333333336',
 '33333333-3333-3333-3333-333333333333',
 'Maharashtra',
 'Mumbai',
 '',
 true,
 '00000000-0000-0000-0000-000000000000',
 '00000000-0000-0000-0000-000000000000',
 NOW(),
 NOW());