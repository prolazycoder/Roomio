import os
import re
from typing import Optional
from fastapi import FastAPI, UploadFile, File, Form, HTTPException, status
from pydantic import BaseModel, Field, EmailStr
from dotenv import load_dotenv

load_dotenv()

app = FastAPI(
    title="HostelApp GenAI & Automation Service",
    description="FastAPI service for document metadata extraction (OCR) and WhatsApp webhook handling.",
    version="1.0.0"
)

# --- GenAI OCR Extraction schemas ---

class ExtractedLeaseMetadata(BaseModel):
    tenant_name: str = Field(..., description="Full name of the tenant")
    tenant_email: EmailStr = Field(..., description="Email address of the tenant")
    rent_amount: float = Field(..., description="Monthly rent amount specified in the lease")
    start_date: str = Field(..., description="Lease start date (YYYY-MM-DD)")
    end_date: Optional[str] = Field(None, description="Lease end date if FIXED_PERIOD (YYYY-MM-DD)")
    gov_id_type: str = Field(..., description="Type of government ID, e.g. Passport, Aadhaar")
    gov_id_number_masked: str = Field(..., description="Natively masked government ID number")

    class Config:
        json_schema_extra = {
            "example": {
                "tenant_name": "John Doe",
                "tenant_email": "johndoe@example.com",
                "rent_amount": 1500.0,
                "start_date": "2026-07-01",
                "end_date": "2027-06-30",
                "gov_id_type": "Passport",
                "gov_id_number_masked": "******7890"
            }
        }

def mask_sensitive_value(value: str) -> str:
    """Natively masks government ID values to ensure PII safety."""
    if not value:
        return ""
    clean_val = value.strip()
    if len(clean_val) <= 4:
        return "****"
    return "*" * (len(clean_val) - 4) + clean_val[-4:]

@app.post("/ocr/extract", response_model=ExtractedLeaseMetadata, tags=["GenAI OCR"])
async def extract_lease_document(file: UploadFile = File(...)):
    """
    Simulates scanned lease document OCR parsing & metadata extraction.
    Integrates with OpenAI structured outputs (via client.beta.chat.completions.parse)
    and natively masks sensitive PII data before returning.
    """
    # 1. Validate file extension
    file_ext = os.path.splitext(file.filename)[1].lower()
    if file_ext not in [".pdf", ".png", ".jpg", ".jpeg"]:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Unsupported file format. Please upload a PDF or an Image file."
        )
    
    # 2. (Mock Pipeline) Reading the file contents
    content = await file.read()
    
    # 3. Simulate GenAI structured output parsing.
    # In a real integration, you would write:
    # client = OpenAI()
    # completion = client.beta.chat.completions.parse(
    #     model="gpt-4o-2024-08-06",
    #     messages=[...],
    #     response_format=ExtractedLeaseMetadata,
    # )
    # response_data = completion.choices[0].message.parsed
    
    # Let's mock the extraction of raw OCR text and conversion to our model:
    raw_extracted_gov_id = "US-98765-ABC-3210" # Sensitive PII extracted by OCR
    masked_gov_id = mask_sensitive_value(raw_extracted_gov_id)

    mocked_metadata = ExtractedLeaseMetadata(
        tenant_name="Alice Smith",
        tenant_email="alice.smith@example.com",
        rent_amount=1200.00,
        start_date="2026-07-01",
        end_date=None, # Monthly rolling contract
        gov_id_type="Passport",
        gov_id_number_masked=masked_gov_id # Natively masked
    )

    return mocked_metadata


# --- WhatsApp Twilio Mock Webhook ---

class TwilioWhatsAppWebhookPayload(BaseModel):
    MessageSid: str = Field(..., description="Unique message identifier from Twilio")
    From: str = Field(..., description="Sender WhatsApp identifier in format 'whatsapp:+1234567890'")
    To: str = Field(..., description="Receiver WhatsApp identifier in format 'whatsapp:+1234567890'")
    Body: str = Field(..., description="Content of the incoming WhatsApp message")
    NumMedia: int = Field(0, description="Number of media attachments")

@app.post("/whatsapp/webhook", status_code=status.HTTP_200_OK, tags=["Notifications"])
async def receive_whatsapp_message(
    MessageSid: str = Form(...),
    From: str = Form(...),
    To: str = Form(...),
    Body: str = Form(...),
    NumMedia: int = Form(0)
):
    """
    Twilio-compatible mock webhook for receiving WhatsApp events.
    Analyzes message body for payment confirmations and returns event-driven alerts.
    """
    # 1. Parse payload parameters (incoming from Twilio as form-urlencoded)
    payload = TwilioWhatsAppWebhookPayload(
        MessageSid=MessageSid,
        From=From,
        To=To,
        Body=Body,
        NumMedia=NumMedia
    )

    # 2. Simple NLP/Regex to detect payment alerts (e.g., "Confirm payment for Invoice #102")
    match = re.search(r"invoice\s*#?\s*(\d+)", payload.Body, re.IGNORECASE)
    
    response_msg = "Hello! We received your message. For payment queries, please log in to the tenant portal."
    
    if match:
        invoice_id = match.group(1)
        response_msg = f"Thank you! We have logged your request regarding Invoice #{invoice_id}. Our team is verifying the payment reference."

    # Return Twilio-style TwiML response as XML or simple JSON response mock
    return {
        "status": "success",
        "processed_message_sid": payload.MessageSid,
        "reply_sent": response_msg
    }

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)
