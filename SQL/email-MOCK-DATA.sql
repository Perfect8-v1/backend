-- ================================================
-- email-MOCK-DATA.sql
-- Database: emailDB
-- Created: 2025-11-16
-- Purpose: Mock data for email-service testing
-- 
-- IMPORTANT NOTES:
-- - 7 HTML email templates (ORDER, CUSTOMER, MARKETING, BLOG)
-- - Template variables: {{customerName}}, {{orderNumber}}, etc.
-- - Swedish email content
-- - Matches actual HTML files in email-service/resources/templates/
-- ================================================

-- ================================================
-- TRUNCATE TABLE (Safe reload without duplicates)
-- ================================================

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE email_templates;
SET FOREIGN_KEY_CHECKS = 1;

-- ================================================
-- INSERT MOCK DATA: email_templates
-- ================================================

INSERT INTO email_templates (
    name,
    subject,
    content,
    html_content,
    description,
    template_type,
    category,
    active,
    version,
    created_by,
    updated_by,
    created_date,
    updated_date,
    required_variables,
    optional_variables,
    usage_count,
    last_used_date
) VALUES

-- ================================================
-- ORDER EMAILS
-- ================================================

(
    'order-confirmation',
    'Din beställning är bekräftad - Order {{orderNumber}}',
    'Hej {{customerName}},

Tack för din beställning! Vi har tagit emot order {{orderNumber}} och förbereder den för leverans.

Ordersumma: {{totalAmount}} SEK
Betalningsmetod: {{paymentMethod}}
Leveransadress: {{shippingAddress}}

Vi skickar ett nytt email när din order har skickats.

Med vänliga hälsningar,
Perfect8 Team',
    '<!DOCTYPE html>
<html lang="sv">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Orderbekräftelse</title>
</head>
<body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px;">
    <div style="background-color: #4CAF50; color: white; padding: 20px; text-align: center;">
        <h1 style="margin: 0;">Perfect8</h1>
        <p style="margin: 5px 0 0 0;">Orderbekräftelse</p>
    </div>
    
    <div style="background-color: #f9f9f9; padding: 20px; margin-top: 20px;">
        <h2 style="color: #4CAF50;">Tack för din beställning, {{customerName}}!</h2>
        <p>Vi har tagit emot din order och förbereder den för leverans.</p>
        
        <div style="background-color: white; padding: 15px; margin: 20px 0; border-left: 4px solid #4CAF50;">
            <p style="margin: 0;"><strong>Ordernummer:</strong> {{orderNumber}}</p>
            <p style="margin: 5px 0 0 0;"><strong>Orderdatum:</strong> {{orderDate}}</p>
        </div>
        
        <h3 style="color: #333; margin-top: 30px;">Orderdetaljer</h3>
        <table style="width: 100%; border-collapse: collapse; margin: 10px 0;">
            <thead>
                <tr style="background-color: #f0f0f0;">
                    <th style="padding: 10px; text-align: left; border-bottom: 2px solid #ddd;">Produkt</th>
                    <th style="padding: 10px; text-align: center; border-bottom: 2px solid #ddd;">Antal</th>
                    <th style="padding: 10px; text-align: right; border-bottom: 2px solid #ddd;">Pris</th>
                </tr>
            </thead>
            <tbody>
                {{#orderItems}}
                <tr>
                    <td style="padding: 10px; border-bottom: 1px solid #eee;">{{productName}}</td>
                    <td style="padding: 10px; text-align: center; border-bottom: 1px solid #eee;">{{quantity}}</td>
                    <td style="padding: 10px; text-align: right; border-bottom: 1px solid #eee;">{{price}} SEK</td>
                </tr>
                {{/orderItems}}
            </tbody>
        </table>
        
        <div style="text-align: right; margin-top: 20px; padding-top: 10px; border-top: 2px solid #ddd;">
            <p style="margin: 5px 0;"><strong>Delsumma:</strong> {{subtotal}} SEK</p>
            <p style="margin: 5px 0;"><strong>Frakt:</strong> {{shippingCost}} SEK</p>
            <p style="margin: 5px 0;"><strong>Moms (25%):</strong> {{taxAmount}} SEK</p>
            <p style="margin: 10px 0 0 0; font-size: 18px; color: #4CAF50;"><strong>Totalt:</strong> {{totalAmount}} SEK</p>
        </div>
        
        <h3 style="color: #333; margin-top: 30px;">Leveransadress</h3>
        <p style="margin: 5px 0;">{{shippingAddress}}</p>
        
        <h3 style="color: #333; margin-top: 30px;">Betalningsmetod</h3>
        <p style="margin: 5px 0;">{{paymentMethod}}</p>
    </div>
    
    <div style="background-color: #f0f0f0; padding: 15px; margin-top: 20px; text-align: center; font-size: 12px; color: #666;">
        <p style="margin: 0;">Vi skickar ett nytt email när din order har skickats.</p>
        <p style="margin: 10px 0 0 0;">Frågor? Kontakta oss på <a href="mailto:support@perfect8.se" style="color: #4CAF50;">support@perfect8.se</a></p>
    </div>
</body>
</html>',
    'Orderbekräftelse som skickas direkt när kund lägger en order',
    'TRANSACTIONAL',
    'ORDER',
    TRUE,
    3,
    'system',
    'magnus.b',
    '2025-01-15 10:00:00.000000',
    '2025-11-10 14:30:00.000000',
    'customerName,orderNumber,orderDate,orderItems,subtotal,shippingCost,taxAmount,totalAmount,shippingAddress,paymentMethod',
    'customerEmail,trackingUrl',
    1847,
    '2025-11-16 09:45:00.000000'
),

(
    'order-shipped',
    'Din order har skickats - Order {{orderNumber}}',
    'Hej {{customerName}},

Goda nyheter! Din order {{orderNumber}} har nu skickats.

Spårningsnummer: {{trackingNumber}}
Transportör: {{carrier}}
Beräknad leverans: {{estimatedDelivery}}

Spåra din leverans: {{trackingUrl}}

Med vänliga hälsningar,
Perfect8 Team',
    '<!DOCTYPE html>
<html lang="sv">
<head>
    <meta charset="UTF-8">
    <title>Order skickad</title>
</head>
<body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
    <div style="background-color: #2196F3; color: white; padding: 20px; text-align: center;">
        <h1 style="margin: 0;">Perfect8</h1>
        <p style="margin: 5px 0 0 0;">Din order är på väg!</p>
    </div>
    
    <div style="padding: 20px;">
        <h2 style="color: #2196F3;">Hej {{customerName}}!</h2>
        <p>Goda nyheter! Din order <strong>{{orderNumber}}</strong> har nu skickats och är på väg till dig.</p>
        
        <div style="background-color: #e3f2fd; padding: 20px; margin: 20px 0; border-radius: 5px;">
            <p style="margin: 5px 0;"><strong>Spårningsnummer:</strong> {{trackingNumber}}</p>
            <p style="margin: 5px 0;"><strong>Transportör:</strong> {{carrier}}</p>
            <p style="margin: 5px 0;"><strong>Beräknad leverans:</strong> {{estimatedDelivery}}</p>
        </div>
        
        <div style="text-align: center; margin: 30px 0;">
            <a href="{{trackingUrl}}" style="background-color: #2196F3; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; display: inline-block;">Spåra min leverans</a>
        </div>
    </div>
    
    <div style="background-color: #f0f0f0; padding: 15px; text-align: center; font-size: 12px; color: #666;">
        <p style="margin: 0;">Frågor om din leverans? Kontakta oss på support@perfect8.se</p>
    </div>
</body>
</html>',
    'Leveransbekräftelse när order har skickats',
    'TRANSACTIONAL',
    'ORDER',
    TRUE,
    2,
    'system',
    'sara.dev',
    '2025-01-20 11:00:00.000000',
    '2025-10-05 16:20:00.000000',
    'customerName,orderNumber,trackingNumber,carrier,estimatedDelivery,trackingUrl',
    'shippingAddress',
    923,
    '2025-11-16 08:30:00.000000'
),

(
    'order-cancelled',
    'Din order har återställts - Order {{orderNumber}}',
    'Hej {{customerName}},

Din order {{orderNumber}} har återställts enligt din begäran.

Återbetalning: {{refundAmount}} SEK
Betalningsmetod: {{paymentMethod}}
Beräknad återbetalningstid: 5-7 arbetsdagar

Om du har frågor, tveka inte att kontakta oss.

Med vänliga hälsningar,
Perfect8 Team',
    '<!DOCTYPE html>
<html lang="sv">
<head>
    <meta charset="UTF-8">
    <title>Order återställd</title>
</head>
<body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
    <div style="background-color: #FF9800; color: white; padding: 20px; text-align: center;">
        <h1 style="margin: 0;">Perfect8</h1>
        <p style="margin: 5px 0 0 0;">Orderåterställning</p>
    </div>
    
    <div style="padding: 20px;">
        <h2 style="color: #FF9800;">Order återställd</h2>
        <p>Hej {{customerName}},</p>
        <p>Din order <strong>{{orderNumber}}</strong> har återställts enligt din begäran.</p>
        
        <div style="background-color: #fff3e0; padding: 20px; margin: 20px 0; border-left: 4px solid #FF9800;">
            <p style="margin: 5px 0;"><strong>Återbetalning:</strong> {{refundAmount}} SEK</p>
            <p style="margin: 5px 0;"><strong>Betalningsmetod:</strong> {{paymentMethod}}</p>
            <p style="margin: 5px 0;"><strong>Beräknad återbetalningstid:</strong> 5-7 arbetsdagar</p>
        </div>
        
        <p><strong>Anledning:</strong> {{cancellationReason}}</p>
    </div>
    
    <div style="background-color: #f0f0f0; padding: 15px; text-align: center; font-size: 12px; color: #666;">
        <p style="margin: 0;">Frågor? Kontakta oss på support@perfect8.se</p>
    </div>
</body>
</html>',
    'Bekräftelse när order har återställts',
    'TRANSACTIONAL',
    'ORDER',
    TRUE,
    1,
    'system',
    'system',
    '2025-01-25 12:00:00.000000',
    '2025-08-15 10:00:00.000000',
    'customerName,orderNumber,refundAmount,paymentMethod,cancellationReason',
    'orderDate',
    87,
    '2025-11-10 15:20:00.000000'
),

-- ================================================
-- CUSTOMER EMAILS
-- ================================================

(
    'welcome',
    'Välkommen till Perfect8, {{customerName}}!',
    'Hej {{customerName}},

Välkommen till Perfect8! Vi är glada att ha dig som kund.

För att komma igång:
1. Verifiera din email genom att klicka på länken nedan
2. Utforska vårt sortiment
3. Njut av fri frakt på beställningar över 500 SEK

Verifiera email: {{verificationUrl}}

Med vänliga hälsningar,
Perfect8 Team',
    '<!DOCTYPE html>
<html lang="sv">
<head>
    <meta charset="UTF-8">
    <title>Välkommen</title>
</head>
<body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
    <div style="background-color: #9C27B0; color: white; padding: 30px; text-align: center;">
        <h1 style="margin: 0; font-size: 32px;">Välkommen till Perfect8!</h1>
    </div>
    
    <div style="padding: 30px;">
        <h2 style="color: #9C27B0;">Hej {{customerName}}!</h2>
        <p>Vi är glada att ha dig som kund hos Perfect8.</p>
        
        <h3>Kom igång:</h3>
        <ol style="line-height: 1.8;">
            <li>Verifiera din email-adress</li>
            <li>Utforska vårt sortiment</li>
            <li>Njut av fri frakt på beställningar över 500 SEK</li>
        </ol>
        
        <div style="text-align: center; margin: 30px 0;">
            <a href="{{verificationUrl}}" style="background-color: #9C27B0; color: white; padding: 15px 40px; text-decoration: none; border-radius: 5px; display: inline-block; font-weight: bold;">Verifiera min email</a>
        </div>
        
        <p style="color: #666; font-size: 12px; margin-top: 20px;">Om du inte registrerade dig på Perfect8, kan du ignorera detta email.</p>
    </div>
    
    <div style="background-color: #f0f0f0; padding: 15px; text-align: center; font-size: 12px; color: #666;">
        <p style="margin: 0;">Perfect8 - Din tech-partner</p>
    </div>
</body>
</html>',
    'Välkomstmail för nya kunder',
    'TRANSACTIONAL',
    'CUSTOMER',
    TRUE,
    2,
    'system',
    'anna.code',
    '2025-02-01 09:00:00.000000',
    '2025-09-20 11:30:00.000000',
    'customerName,verificationUrl',
    'customerEmail',
    512,
    '2025-11-16 07:15:00.000000'
),

(
    'password-reset',
    'Återställ ditt lösenord - Perfect8',
    'Hej {{customerName}},

Vi har fått en begäran om att återställa lösenordet för ditt konto.

Om du gjorde denna begäran, klicka på länken nedan:
{{resetUrl}}

Länken är giltig i 1 timme.

Om du inte begärde en lösenordsåterställning, kan du ignorera detta email.

Med vänliga hälsningar,
Perfect8 Team',
    '<!DOCTYPE html>
<html lang="sv">
<head>
    <meta charset="UTF-8">
    <title>Lösenordsåterställning</title>
</head>
<body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
    <div style="background-color: #F44336; color: white; padding: 20px; text-align: center;">
        <h1 style="margin: 0;">Perfect8</h1>
        <p style="margin: 5px 0 0 0;">Lösenordsåterställning</p>
    </div>
    
    <div style="padding: 20px;">
        <h2 style="color: #F44336;">Återställ ditt lösenord</h2>
        <p>Hej {{customerName}},</p>
        <p>Vi har fått en begäran om att återställa lösenordet för ditt konto.</p>
        
        <div style="background-color: #ffebee; padding: 20px; margin: 20px 0; border-left: 4px solid #F44336;">
            <p style="margin: 0;"><strong>⚠️ Säkerhetsvarning</strong></p>
            <p style="margin: 5px 0 0 0;">Om du inte begärde denna återställning, ignorera detta email och ditt lösenord förblir oförändrat.</p>
        </div>
        
        <div style="text-align: center; margin: 30px 0;">
            <a href="{{resetUrl}}" style="background-color: #F44336; color: white; padding: 15px 40px; text-decoration: none; border-radius: 5px; display: inline-block; font-weight: bold;">Återställ lösenord</a>
        </div>
        
        <p style="font-size: 12px; color: #666;">Denna länk är giltig i 1 timme från det att emailet skickades.</p>
    </div>
    
    <div style="background-color: #f0f0f0; padding: 15px; text-align: center; font-size: 12px; color: #666;">
        <p style="margin: 0;">Perfect8 - Vi skickar aldrig emails där vi ber om ditt lösenord</p>
    </div>
</body>
</html>',
    'Lösenordsåterställning via email',
    'TRANSACTIONAL',
    'CUSTOMER',
    TRUE,
    3,
    'system',
    'magnus.b',
    '2025-02-10 10:30:00.000000',
    '2025-11-01 09:00:00.000000',
    'customerName,resetUrl',
    'customerEmail',
    234,
    '2025-11-15 18:30:00.000000'
),

-- ================================================
-- MARKETING EMAILS
-- ================================================

(
    'newsletter',
    'Perfect8 Nyhetsbrev - {{month}} {{year}}',
    'Hej {{customerName}},

Här är vad som är nytt på Perfect8 denna månad:

{{newsItems}}

Missa inte våra kampanjer:
{{promotions}}

Besök vår webbshop: {{shopUrl}}

Avsluta prenumeration: {{unsubscribeUrl}}

Med vänliga hälsningar,
Perfect8 Team',
    '<!DOCTYPE html>
<html lang="sv">
<head>
    <meta charset="UTF-8">
    <title>Nyhetsbrev</title>
</head>
<body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
    <div style="background-color: #00BCD4; color: white; padding: 20px; text-align: center;">
        <h1 style="margin: 0;">Perfect8 Nyhetsbrev</h1>
        <p style="margin: 5px 0 0 0;">{{month}} {{year}}</p>
    </div>
    
    <div style="padding: 20px;">
        <h2>Hej {{customerName}}!</h2>
        <p>Här är vad som är nytt på Perfect8 denna månad.</p>
        
        <h3 style="color: #00BCD4;">Nyheter</h3>
        <div>{{newsItems}}</div>
        
        <h3 style="color: #00BCD4; margin-top: 30px;">Kampanjer</h3>
        <div>{{promotions}}</div>
        
        <div style="text-align: center; margin: 40px 0;">
            <a href="{{shopUrl}}" style="background-color: #00BCD4; color: white; padding: 15px 40px; text-decoration: none; border-radius: 5px; display: inline-block; font-weight: bold;">Besök webbshopen</a>
        </div>
    </div>
    
    <div style="background-color: #f0f0f0; padding: 15px; text-align: center; font-size: 12px; color: #666;">
        <p style="margin: 0;">Du får detta email för att du prenumererar på Perfect8 nyhetsbrev.</p>
        <p style="margin: 5px 0 0 0;"><a href="{{unsubscribeUrl}}" style="color: #666;">Avsluta prenumeration</a></p>
    </div>
</body>
</html>',
    'Månatligt nyhetsbrev till prenumeranter',
    'MARKETING',
    'MARKETING',
    TRUE,
    5,
    'marketing',
    'sara.dev',
    '2025-03-01 08:00:00.000000',
    '2025-11-01 10:00:00.000000',
    'customerName,month,year,newsItems,promotions,shopUrl,unsubscribeUrl',
    'campaignId',
    3421,
    '2025-11-01 06:00:00.000000'
),

-- ================================================
-- BLOG EMAILS
-- ================================================

(
    'new-post-notification',
    'Nytt blogginlägg: {{postTitle}}',
    'Hej {{subscriberName}},

{{authorName}} har publicerat ett nytt blogginlägg på Perfect8:

{{postTitle}}

{{excerpt}}

Läs mer: {{postUrl}}

Avsluta prenumeration: {{unsubscribeUrl}}

Med vänliga hälsningar,
Perfect8 Blog Team',
    '<!DOCTYPE html>
<html lang="sv">
<head>
    <meta charset="UTF-8">
    <title>Nytt blogginlägg</title>
</head>
<body style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;">
    <div style="background-color: #673AB7; color: white; padding: 20px; text-align: center;">
        <h1 style="margin: 0;">Perfect8 Blog</h1>
        <p style="margin: 5px 0 0 0;">Nytt inlägg publicerat</p>
    </div>
    
    <div style="padding: 20px;">
        <p>Hej {{subscriberName}}!</p>
        <p><strong>{{authorName}}</strong> har publicerat ett nytt blogginlägg:</p>
        
        <div style="background-color: #ede7f6; padding: 20px; margin: 20px 0; border-left: 4px solid #673AB7;">
            <h2 style="margin: 0 0 10px 0; color: #673AB7;">{{postTitle}}</h2>
            <p style="margin: 0; color: #666;">{{excerpt}}</p>
        </div>
        
        <div style="text-align: center; margin: 30px 0;">
            <a href="{{postUrl}}" style="background-color: #673AB7; color: white; padding: 15px 40px; text-decoration: none; border-radius: 5px; display: inline-block; font-weight: bold;">Läs hela inlägget</a>
        </div>
    </div>
    
    <div style="background-color: #f0f0f0; padding: 15px; text-align: center; font-size: 12px; color: #666;">
        <p style="margin: 0;">Du får detta email för att du prenumererar på Perfect8 Blog.</p>
        <p style="margin: 5px 0 0 0;"><a href="{{unsubscribeUrl}}" style="color: #666;">Avsluta prenumeration</a></p>
    </div>
</body>
</html>',
    'Notifikation när nytt blogginlägg publiceras',
    'MARKETING',
    'BLOG',
    TRUE,
    2,
    'system',
    'johan.spring',
    '2025-04-01 11:00:00.000000',
    '2025-10-15 14:00:00.000000',
    'subscriberName,authorName,postTitle,excerpt,postUrl,unsubscribeUrl',
    'postId,publishDate',
    645,
    '2025-11-13 15:30:00.000000'
);

-- ================================================
-- VERIFICATION QUERY (uncomment to test)
-- ================================================

-- SELECT 
--     email_template_id,
--     name,
--     subject,
--     template_type,
--     category,
--     active,
--     version,
--     usage_count,
--     DATE_FORMAT(created_date, '%Y-%m-%d') AS created,
--     DATE_FORMAT(last_used_date, '%Y-%m-%d %H:%i') AS last_used,
--     created_by,
--     updated_by
-- FROM email_templates
-- ORDER BY category, name;

-- ================================================
-- End of email-MOCK-DATA.sql
-- 
-- SUMMARY:
-- - 7 email templates (ORDER: 3, CUSTOMER: 2, MARKETING: 1, BLOG: 1)
-- - Complete HTML templates with Swedish content
-- - Template variables for personalization
-- - Usage tracking with realistic counts
-- - Active status and versioning
-- - Ready for frontend email preview testing
-- ================================================
