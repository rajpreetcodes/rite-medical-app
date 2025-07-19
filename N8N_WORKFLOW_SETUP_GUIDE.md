# N8N Customer Notification Workflow Setup Guide
## Rite Medical Application - Order Status Notifications

### Overview
This workflow automatically sends email notifications to customers when their order status changes in the Rite Medical Application. The workflow handles 6 different order statuses: CONFIRMED, PAYMENT_RECEIVED, PROCESSING, SHIPPED, DELIVERED, and CANCELLED.

---

## 🔧 PREREQUISITES & CREDENTIALS SETUP

### 1. Required Accounts & APIs
Before setting up this workflow, you need:

#### A. Gmail Account Setup
- **Gmail Account**: Create a dedicated Gmail account for sending notifications (e.g., `notifications@yourmedicalapp.com`)
- **Gmail API**: Enable Gmail API in Google Cloud Console
- **App Password**: Generate an app password for the Gmail account (2FA must be enabled)

#### B. Google Cloud Console Setup
- **Project**: Create a new Google Cloud Project or use existing one
- **APIs to Enable**:
  - Gmail API
  - Google Sheets API (if using sheets for customer data)
  - Firebase Admin SDK (if using Firestore)

#### C. Firebase Setup (if using Firestore)
- **Firebase Project**: Create/use existing Firebase project
- **Service Account**: Generate service account key for Firestore access
- **Firestore Database**: Set up collections for orders and customers

---

## 📋 WORKFLOW NODES SETUP GUIDE

### Node 1: Webhook Trigger
**Purpose**: Receives order status updates from your Android app

#### Configuration Steps:
1. **Add Webhook Node**
   - Drag "Webhook" node from trigger section
   - Set HTTP Method: `POST`
   - Set Path: `customer-notification`
   - **IMPORTANT**: Note the webhook URL - you'll need this for your Android app

#### Required Data Format:
```json
{
  "orderId": "ORDER_12345678",
  "orderStatus": "CONFIRMED",
  "customerEmail": "customer@example.com",
  "customerName": "John Doe",
  "orderTotal": 45.99,
  "orderItems": [
    {
      "productName": "Paracetamol 500mg",
      "quantity": 2,
      "price": 5.99
    }
  ]
}
```

#### Client Configuration Notes:
```
🔧 CLIENT SETUP REQUIRED:
- Replace webhook URL in your Android app's CartViewModel.kt
- Update the webhook endpoint in your order placement logic
- Ensure your app sends all required fields (orderId, orderStatus, customerEmail)
```

---

### Node 2: Data Validation (If Node)
**Purpose**: Validates incoming data before processing

#### Configuration Steps:
1. **Add If Node**
   - Connect from Webhook node
   - Set condition: Check if required fields exist
   - Condition: `{{ $json.orderId && $json.orderStatus && $json.customerEmail }}`

#### Client Configuration Notes:
```
🔧 CLIENT SETUP REQUIRED:
- Modify validation conditions based on your data structure
- Add additional validation rules if needed (e.g., email format, order status values)
- Update error handling for invalid data
```

---

### Node 3: Switch Node (Order Status Router)
**Purpose**: Routes to different email templates based on order status

#### Configuration Steps:
1. **Add Switch Node**
   - Connect from If node
   - Set routing field: `{{ $json.orderStatus }}`
   - Add 6 routes (Output 0-5) for each status

#### Route Configuration:
```
Output 0: CONFIRMED
Output 1: PAYMENT_RECEIVED  
Output 2: PROCESSING
Output 3: SHIPPED
Output 4: DELIVERED
Output 5: CANCELLED
```

#### Client Configuration Notes:
```
🔧 CLIENT SETUP REQUIRED:
- Update order status values to match your app's status values
- Add/remove routes if you have different statuses
- Ensure status values are exactly matched (case-sensitive)
```

---

### Node 4-9: Gmail Nodes (Email Templates)
**Purpose**: Send customized emails for each order status

#### Gmail Credentials Setup:
1. **Create Gmail Credentials**
   - Click "Add Credential" in Gmail node
   - Select "Gmail OAuth2 API"
   - Follow OAuth2 setup process
   - **IMPORTANT**: Use the dedicated notification email account

#### Email Template Configuration:

##### Node 4: CONFIRMED Status Email
```
To: {{ $json.customerEmail }}
Subject: Order Confirmed - Rite Medical #{{ $json.orderId }}
Body: [See template below]
```

**Email Template:**
```html
Dear {{ $json.customerName }},

Your order #{{ $json.orderId }} has been confirmed!

Order Details:
- Order ID: {{ $json.orderId }}
- Total Amount: ${{ $json.orderTotal }}
- Status: Confirmed

We're preparing your order and will notify you when payment is received.

Thank you for choosing Rite Medical!

Best regards,
Rite Medical Team
```

##### Node 5: PAYMENT_RECEIVED Status Email
```
To: {{ $json.customerEmail }}
Subject: Payment Received - Order #{{ $json.orderId }}
Body: [Similar template with payment confirmation]
```

##### Node 6: PROCESSING Status Email
```
To: {{ $json.customerEmail }}
Subject: Order Processing - #{{ $json.orderId }}
Body: [Template about order being processed]
```

##### Node 7: SHIPPED Status Email
```
To: {{ $json.customerEmail }}
Subject: Order Shipped - #{{ $json.orderId }}
Body: [Template with delivery tracking info]
```

##### Node 8: DELIVERED Status Email
```
To: {{ $json.customerEmail }}
Subject: Order Delivered - #{{ $json.orderId }}
Body: [Template confirming delivery]
```

##### Node 9: CANCELLED Status Email
```
To: {{ $json.customerEmail }}
Subject: Order Cancelled - #{{ $json.orderId }}
Body: [Template explaining cancellation]
```

#### Client Configuration Notes:
```
🔧 CLIENT SETUP REQUIRED:
- Replace email templates with your brand messaging
- Update sender email address
- Customize email content for each status
- Add your company logo and branding
- Include customer support contact information
- Add tracking links if applicable
- Test email delivery to ensure proper formatting
```

---

## 🔄 OPTIONAL: Customer Data Lookup Node
**Purpose**: Fetch additional customer information from database

#### Configuration Steps:
1. **Add HTTP Request Node** (before Switch node)
   - Method: `GET`
   - URL: `https://your-firebase-project.firebaseio.com/customers/{{ $json.customerId }}.json`
   - Headers: Add Firebase authentication

#### Client Configuration Notes:
```
🔧 CLIENT SETUP REQUIRED:
- Replace Firebase URL with your project URL
- Add Firebase service account credentials
- Update customer data structure
- Modify lookup logic based on your database schema
- Add error handling for missing customer data
```

---

## 🚀 DEPLOYMENT & TESTING

### 1. Activate Workflow
- Click "Active" toggle in n8n
- Workflow will start listening for webhook requests

### 2. Test Workflow
- Use Postman or curl to send test webhook requests
- Verify emails are sent for each status
- Check email formatting and content

### 3. Integration with Android App
- Update your Android app's webhook URL
- Test order placement and status updates
- Monitor workflow execution logs

#### Client Configuration Notes:
```
🔧 CLIENT SETUP REQUIRED:
- Update webhook URL in Android app code
- Test with real order data
- Monitor email delivery rates
- Set up email delivery monitoring
- Configure error notifications for failed emails
```

---

## 📊 MONITORING & MAINTENANCE

### 1. Workflow Monitoring
- Check n8n execution logs regularly
- Monitor email delivery success rates
- Set up alerts for workflow failures

### 2. Email Analytics
- Track email open rates
- Monitor customer engagement
- Analyze delivery timing

### 3. Maintenance Tasks
- Update email templates periodically
- Review and optimize workflow performance
- Backup workflow configurations

#### Client Configuration Notes:
```
🔧 CLIENT SETUP REQUIRED:
- Set up monitoring dashboards
- Configure alert notifications
- Schedule regular workflow reviews
- Plan for email template updates
- Document any customizations made
```

---

## 🔐 SECURITY CONSIDERATIONS

### 1. Webhook Security
- Use HTTPS for webhook endpoints
- Implement webhook authentication
- Validate incoming data

### 2. Email Security
- Use dedicated email account
- Enable 2FA on email account
- Monitor for suspicious activity

### 3. Data Privacy
- Ensure GDPR compliance
- Implement data retention policies
- Secure customer information

#### Client Configuration Notes:
```
🔧 CLIENT SETUP REQUIRED:
- Implement webhook authentication
- Set up email security measures
- Review data privacy compliance
- Configure data retention policies
- Document security procedures
```

---

## 📞 SUPPORT & TROUBLESHOOTING

### Common Issues:
1. **Emails not sending**: Check Gmail credentials and API limits
2. **Webhook not receiving data**: Verify URL and authentication
3. **Wrong email templates**: Check Switch node routing
4. **Missing customer data**: Verify data lookup configuration

### Support Resources:
- n8n Documentation: https://docs.n8n.io/
- Gmail API Documentation: https://developers.google.com/gmail/api
- Firebase Documentation: https://firebase.google.com/docs

#### Client Configuration Notes:
```
🔧 CLIENT SETUP REQUIRED:
- Document your specific configuration
- Create troubleshooting guides
- Set up support contact information
- Plan for workflow updates and maintenance
```

---

## 📝 CHECKLIST FOR CLIENT SETUP

### Before Going Live:
- [ ] Gmail API credentials configured
- [ ] Email templates customized and tested
- [ ] Webhook URL updated in Android app
- [ ] Firebase/database integration tested
- [ ] Email delivery tested for all statuses
- [ ] Monitoring and alerts configured
- [ ] Security measures implemented
- [ ] Documentation completed
- [ ] Support procedures established

### Post-Launch Monitoring:
- [ ] Monitor email delivery rates
- [ ] Track customer engagement
- [ ] Review workflow performance
- [ ] Update templates as needed
- [ ] Maintain security measures

---

**Note**: This workflow is designed as a prototype/demo. For production use, ensure all security measures, error handling, and monitoring are properly configured according to your business requirements. 