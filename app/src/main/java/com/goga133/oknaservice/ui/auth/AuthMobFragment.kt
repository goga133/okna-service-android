package com.goga133.oknaservice.ui.auth

import android.content.ContentValues.TAG
import android.nfc.Tag
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.goga133.oknaservice.R
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.android.synthetic.main.fragment_auth_input_phone.*
import kotlinx.android.synthetic.main.fragment_auth_input_phone.view.*
import java.util.concurrent.TimeUnit


class AuthMobFragment : Fragment() {

    private val mAuth by lazy { FirebaseAuth.getInstance() }

    // Код страны (Например: +7):
    private val countryCode by lazy { getString(R.string.mobile_country_code) }

    private lateinit var root: View
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        root = inflater.inflate(R.layout.fragment_auth_input_phone, container, false)

        root.button_login_mail.setOnClickListener {
            findNavController().navigate(R.id.nav_auth_mail)
        }
        root.button_continue.setOnClickListener {
            // Если нет ошибки, связанной с валидацией номера, отправляем код, иначе - показываем ошибку:
            when {
                root.textInput_phone.text.isNullOrEmpty() -> {
                    showError("Ошибка. Поле для ввода не может быть пусто!")
                }
                !isCorrectRusNumber(root.textInput_phone.text) -> {
                    showError("Ошибка. Введённый номер некорректен!")
                }
                else -> {
                    // Пробуем отослать код:
                    try {
                        sendMobileCode()
                    } catch (e: Exception) {
                        Log.d(TAG, e.toString())
                        showError("Ошибка. Повторите запрос позже!")
                        setLoading(false)
                    }
                }
            }
        }
        return root
    }

    private fun sendMobileCode() {
        setLoading(true)

        val phoneNumber = countryCode + textInput_phone.text

        val phoneAuthProvider = PhoneAuthProvider.getInstance()
        phoneAuthProvider.verifyPhoneNumber(phoneNumber,
            120,
            TimeUnit.SECONDS,
            requireActivity(),
            object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    // Если всё хорошо - выполняем вход в аккаунт ещё до перехода на следующий фрагмент:
                    signInWithPhoneAuthCredential(credential)
                    setLoading(false)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    when (e) {
                        is FirebaseTooManyRequestsException -> showError("Ошибка. Превышен лимит запросов, повторите попытку позже!")
                        is FirebaseNetworkException -> showError("Ошибка. Проверьте подключение к интернету!")
                        else -> {
                            Log.d(TAG, e.toString())
                            showError("Ошибка. Повторите запрос позже!")
                        }
                    }

                    setLoading(false)
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {

                    // Ждём 10 секунд, чтобы код был введён в автоматическом режиме,
                    // иначе вводим код в ручном режиме:
                    super.onCodeSent(verificationId, token)

                        Handler().postDelayed(
                            {
                                if (mAuth.currentUser == null && isVisible) {
                                    setLoading(false)

                                    val navController = root.findNavController()
                                    val args = Bundle().apply {
                                        putString("verificationId", verificationId)
                                        putString("phoneNumber", phoneNumber)
                                    }
                                    navController
                                        .navigate(R.id.nav_auth_code_phone, args)
                                }
                            }, 10000
                        )

                }
            })

    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this.requireActivity()) { task ->
                if (task.isSuccessful && isVisible) {
                    val navController = root.findNavController()

                    // Чистим стек фрагментов:
                    navController.popBackStack(R.id.nav_auth_input_phone, true);
                    navController.popBackStack(R.id.nav_auth, true);
                    // Отправляем на нужный фрагмент:

                    navController.navigate(R.id.nav_personal);


                } else if (task.exception is FirebaseAuthInvalidCredentialsException) {
                    showError("Ошибка. Неверно введённый код.")
                }
            }
    }

    private fun showError(error: String) {
        if(isVisible) {
            root.textView_error.visibility = View.VISIBLE
            root.textView_error.text = error
        }
    }

    // Выставляем ProgressBar и делаем кнопку включённой или нет:
    private fun setLoading(loading: Boolean) {
        if(isVisible) {
            root.button_continue.isEnabled = !loading
            when (loading) {
                true -> root.login_progressbar.visibility = View.VISIBLE
                false -> root.login_progressbar.visibility = View.INVISIBLE
            }
        }
    }

    private fun isCorrectRusNumber(target: CharSequence?): Boolean {
        return target.toString().matches(Regex("\\d{10}"))
    }

}



