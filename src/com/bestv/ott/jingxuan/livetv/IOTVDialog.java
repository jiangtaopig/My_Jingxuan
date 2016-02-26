package com.bestv.ott.jingxuan.livetv;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.TextView;
import com.bestv.jingxuan.R;

public class IOTVDialog extends Dialog {
	public IOTVDialog(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public IOTVDialog(Context context, int theme) {
		super(context, theme);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

//		setContentView(R.layout.gdyd_dialog);
	}

    /**
     * Helper class for creating a custom dialog
     */
    public static class Builder {
 
        private Context context = null;
        private String title = null;
        private String message = null;
        private String positiveButtonText = null;
        private String negativeButtonText = null;
        private View contentView = null;
 
        private DialogInterface.OnClickListener positiveButtonClickListener = null;
        private DialogInterface.OnClickListener negativeButtonClickListener = null;
 
        public Builder(Context context) {
            this.context = context;
        }
 
        /**
         * Set the Dialog message from String
         * @param title
         * @return
         */
        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }
 
        /**
         * Set the Dialog message from resource
         * @param title
         * @return
         */
        public Builder setMessage(int message) {
            this.message = (String) context.getText(message);
            return this;
        }
 
        /**
         * Set the Dialog title from resource
         * @param title
         * @return
         */
        public Builder setTitle(int title) {
            this.title = (String) context.getText(title);
            return this;
        }
 
        /**
         * Set the Dialog title from String
         * @param title
         * @return
         */
        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }
 
        /**
         * Set a custom content view for the Dialog.
         * If a message is set, the contentView is not
         * added to the Dialog...
         * @param v
         * @return
         */
        public Builder setContentView(View v) {
            this.contentView = v;
            return this;
        }
 
        /**
         * Set the positive button resource and it's listener
         * @param positiveButtonText
         * @param listener
         * @return
         */
        public Builder setPositiveButton(int positiveButtonText,
                DialogInterface.OnClickListener listener) {
            this.positiveButtonText = (String) context
                    .getText(positiveButtonText);
            this.positiveButtonClickListener = listener;
            return this;
        }
 
        /**
         * Set the positive button text and it's listener
         * @param positiveButtonText
         * @param listener
         * @return
         */
        public Builder setPositiveButton(String positiveButtonText,
                DialogInterface.OnClickListener listener) {
            this.positiveButtonText = positiveButtonText;
            this.positiveButtonClickListener = listener;
            return this;
        }
 
        /**
         * Set the negative button resource and it's listener
         * @param negativeButtonText
         * @param listener
         * @return
         */
        public Builder setNegativeButton(int negativeButtonText,
                DialogInterface.OnClickListener listener) {
            this.negativeButtonText = (String) context
                    .getText(negativeButtonText);
            this.negativeButtonClickListener = listener;
            return this;
        }
 
        /**
         * Set the negative button text and it's listener
         * @param negativeButtonText
         * @param listener
         * @return
         */
        public Builder setNegativeButton(String negativeButtonText,
                DialogInterface.OnClickListener listener) {
            this.negativeButtonText = negativeButtonText;
            this.negativeButtonClickListener = listener;
            return this;
        }
 
        /**
         * 创建message dialog
         */
        public IOTVDialog createMessage() {
            // instantiate the dialog with the custom Theme
            final IOTVDialog dialog = new IOTVDialog(context, R.style.TVDialog);
            // fill inflater
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.jx_iotv_tv_dialog, null);
            dialog.addContentView(layout, new LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            // set the dialog title
            ((TextView) layout.findViewById(R.id.tv_dialog_title)).setText(title);
            // set the confirm button
            Button positiveButton = (Button) layout.findViewById(R.id.tv_dialog_positive);
            if (positiveButtonText != null) {
            	positiveButton.setText(positiveButtonText);
            	if (positiveButtonClickListener != null) {
            		positiveButton.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    positiveButtonClickListener.onClick(
                                    		dialog, 
                                            DialogInterface.BUTTON_POSITIVE);
                                }
                            });
                }
            } else {
                // if no confirm button just set the visibility to GONE
            	positiveButton.setVisibility(View.GONE);
            }
            // set the cancel button
            Button negativeButton = (Button) layout.findViewById(R.id.tv_dialog_negaitive);
            if (negativeButtonText != null) {
            	negativeButton.setText(negativeButtonText);
                if (negativeButtonClickListener != null) {
                	negativeButton.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                	negativeButtonClickListener.onClick(
                                    		dialog, 
                                            DialogInterface.BUTTON_NEGATIVE);
                                }
                            });
                }
            } else {
                // if no confirm button just set the visibility to GONE
            	negativeButton.setVisibility(View.GONE);
            }
            // set the content message
            if (message != null) {
                ((TextView) layout.findViewById(R.id.tv_dialog_msg)).setText(message);
            } else if (contentView != null) {
                // if no message set
                // add the contentView to the dialog body
//                ((LinearLayout) layout.findViewById(R.id.content))
//                        .removeAllViews();
//                ((LinearLayout) layout.findViewById(R.id.content))
//                        .addView(contentView, 
//                                new LayoutParams(
//                                        LayoutParams.WRAP_CONTENT, 
//                                        LayoutParams.WRAP_CONTENT));
            }
            dialog.setContentView(layout);
            return dialog;
        }
    }
}
