package br.com.hospitalidadeabordo.driver

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.ArrayAdapter
import android.widget.TextView
import java.text.NumberFormat
import java.util.Locale

class CostProfileActivity : Activity() {
    private val prefs by lazy { getSharedPreferences(MainActivity.PREFS, MODE_PRIVATE) }
    private val fields = mutableMapOf<String, EditText>()
    private lateinit var fuelSpinner: Spinner
    private lateinit var summary: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(buildUi())
        refreshSummary()
    }

    private fun buildUi(): ScrollView {
        val scroll = ScrollView(this)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(26), dp(24), dp(40))
            setBackgroundColor(Color.rgb(246, 242, 232))
        }

        root.addView(TextView(this).apply {
            text = "Veículo, custos e metas"
            textSize = 26f
            setTextColor(Color.rgb(23, 41, 56))
        })
        root.addView(TextView(this).apply {
            text = "Base para calcular lucro previsto e realizado"
            textSize = 14f
            setTextColor(Color.DKGRAY)
            setPadding(0, dp(4), 0, dp(16))
        })

        root.addView(sectionTitle("Rotina de trabalho"))
        addNumberField(root, "Dias trabalhados por semana", KEY_DAYS_WEEK, "5")
        addNumberField(root, "Horas dirigidas por dia", KEY_HOURS_DAY, "8")
        addNumberField(root, "Km rodados por dia", KEY_KM_DAY, "150")
        addMoneyField(root, "Meta semanal de faturamento", KEY_WEEKLY_GOAL, "3000")

        root.addView(sectionTitle("Custos fixos mensais"))
        addMoneyField(root, "Aluguel / parcela", KEY_RENT_MONTH, "0")
        addMoneyField(root, "Manutenção provisionada", KEY_MAINTENANCE_MONTH, "0")
        addMoneyField(root, "Seguro, telefone, lavagem e outros", KEY_OTHER_MONTH, "0")

        root.addView(sectionTitle("Energia / combustível"))
        fuelSpinner = Spinner(this)
        val fuels = listOf("Gasolina", "Etanol", "GNV", "Elétrico")
        fuelSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, fuels)
        fuelSpinner.setSelection(fuels.indexOf(prefs.getString(KEY_FUEL_TYPE, "Gasolina")).coerceAtLeast(0))
        root.addView(fuelSpinner, fullWidth())
        addNumberField(root, "Autonomia (km por litro, m³ ou kWh)", KEY_EFFICIENCY, "10")
        addMoneyField(root, "Preço por litro, m³ ou kWh", KEY_ENERGY_PRICE, "0")

        root.addView(sectionTitle("Resultado calculado"))
        summary = infoCard()
        root.addView(summary, fullWidth())

        root.addView(Button(this).apply {
            text = "Salvar e aplicar aos cálculos de oferta"
            isAllCaps = false
            textSize = 16f
            setOnClickListener {
                saveProfile()
                refreshSummary()
            }
            layoutParams = fullWidth(top = 14)
        })

        root.addView(TextView(this).apply {
            text = "O custo por km calculado entra automaticamente no leitor de ofertas. Mais adiante, km e horas estimados serão substituídos pelos valores reais coletados pelo tablet."
            textSize = 13f
            setTextColor(Color.GRAY)
            setPadding(0, dp(18), 0, 0)
        })

        scroll.addView(root)
        return scroll
    }

    private fun addNumberField(root: LinearLayout, label: String, key: String, default: String) {
        root.addView(fieldLabel(label))
        val edit = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            setText(prefs.getString(key, default)?.replace('.', ','))
            textSize = 16f
            setPadding(dp(12), dp(10), dp(12), dp(10))
            setBackgroundColor(Color.WHITE)
        }
        fields[key] = edit
        root.addView(edit, fullWidth())
    }

    private fun addMoneyField(root: LinearLayout, label: String, key: String, default: String) {
        addNumberField(root, label, key, default)
    }

    private fun fieldLabel(value: String) = TextView(this).apply {
        text = value
        textSize = 15f
        setTextColor(Color.DKGRAY)
        setPadding(0, dp(12), 0, dp(4))
    }

    private fun saveProfile() {
        val edit = prefs.edit()
        fields.forEach { (key, field) ->
            parse(field.text.toString())?.let { edit.putString(key, it.toString()) }
        }
        edit.putString(KEY_FUEL_TYPE, fuelSpinner.selectedItem?.toString() ?: "Gasolina")
        edit.apply()

        val result = calculate()
        prefs.edit()
            .putString(MainActivity.KEY_COST_PER_KM, result.totalCostPerKm.toString())
            .putString(KEY_COST_PER_HOUR, result.costPerHour.toString())
            .putString(KEY_FIXED_COST_PER_KM, result.fixedCostPerKm.toString())
            .putString(KEY_ENERGY_COST_PER_KM, result.energyCostPerKm.toString())
            .apply()
    }

    private fun refreshSummary() {
        if (!::summary.isInitialized) return
        val r = calculate()
        summary.text = buildString {
            append("Custo total estimado: ${money(r.totalCostPerKm)}/km\n")
            append("Custo estimado: ${money(r.costPerHour)}/h\n")
            append("Energia/combustível: ${money(r.energyCostPerKm)}/km\n")
            append("Fixos rateados: ${money(r.fixedCostPerKm)}/km\n")
            append("Km previstos/mês: ${format1(r.monthlyKm)}\n")
            append("Horas previstas/mês: ${format1(r.monthlyHours)}\n")
            append("Custo operacional semanal estimado: ${money(r.weeklyOperatingCost)}\n")
            append("Meta semanal: ${money(r.weeklyGoal)}\n")
            append("Margem prevista sobre a meta: ${money(r.weeklyGoal - r.weeklyOperatingCost)}")
        }
    }

    private fun calculate(): Result {
        fun value(key: String, default: Double): Double {
            val live = fields[key]?.text?.toString()?.let(::parse)
            return live ?: prefs.getString(key, default.toString())?.toDoubleOrNull() ?: default
        }

        val days = value(KEY_DAYS_WEEK, 5.0).coerceIn(1.0, 7.0)
        val hoursDay = value(KEY_HOURS_DAY, 8.0).coerceAtLeast(0.1)
        val kmDay = value(KEY_KM_DAY, 150.0).coerceAtLeast(0.0)
        val weeklyGoal = value(KEY_WEEKLY_GOAL, 3000.0).coerceAtLeast(0.0)
        val rent = value(KEY_RENT_MONTH, 0.0).coerceAtLeast(0.0)
        val maintenance = value(KEY_MAINTENANCE_MONTH, 0.0).coerceAtLeast(0.0)
        val other = value(KEY_OTHER_MONTH, 0.0).coerceAtLeast(0.0)
        val efficiency = value(KEY_EFFICIENCY, 10.0).coerceAtLeast(0.01)
        val price = value(KEY_ENERGY_PRICE, 0.0).coerceAtLeast(0.0)

        val weeksPerMonth = 4.345
        val monthlyKm = days * kmDay * weeksPerMonth
        val monthlyHours = days * hoursDay * weeksPerMonth
        val fixedMonthly = rent + maintenance + other
        val energyCostPerKm = price / efficiency
        val fixedCostPerKm = if (monthlyKm > 0) fixedMonthly / monthlyKm else 0.0
        val totalCostPerKm = energyCostPerKm + fixedCostPerKm
        val avgKmPerHour = kmDay / hoursDay
        val fixedCostPerHour = if (monthlyHours > 0) fixedMonthly / monthlyHours else 0.0
        val costPerHour = fixedCostPerHour + energyCostPerKm * avgKmPerHour
        val weeklyHours = days * hoursDay
        val weeklyOperatingCost = weeklyHours * costPerHour

        return Result(
            monthlyKm = monthlyKm,
            monthlyHours = monthlyHours,
            energyCostPerKm = energyCostPerKm,
            fixedCostPerKm = fixedCostPerKm,
            totalCostPerKm = totalCostPerKm,
            costPerHour = costPerHour,
            weeklyOperatingCost = weeklyOperatingCost,
            weeklyGoal = weeklyGoal
        )
    }

    private fun parse(raw: String): Double? = raw.trim().replace("R$", "").replace(" ", "")
        .replace(".", "").replace(",", ".").toDoubleOrNull()

    private fun money(value: Double): String = NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(value)
    private fun format1(value: Double): String = String.format(Locale("pt", "BR"), "%.1f", value)

    private fun sectionTitle(value: String) = TextView(this).apply {
        text = value
        textSize = 19f
        setTextColor(Color.rgb(23, 41, 56))
        setPadding(0, dp(24), 0, dp(8))
    }

    private fun infoCard() = TextView(this).apply {
        textSize = 16f
        setTextColor(Color.rgb(23, 41, 56))
        setPadding(dp(16), dp(16), dp(16), dp(16))
        setBackgroundColor(Color.WHITE)
    }

    private fun fullWidth(top: Int = 0) = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
    ).apply { topMargin = dp(top) }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    data class Result(
        val monthlyKm: Double,
        val monthlyHours: Double,
        val energyCostPerKm: Double,
        val fixedCostPerKm: Double,
        val totalCostPerKm: Double,
        val costPerHour: Double,
        val weeklyOperatingCost: Double,
        val weeklyGoal: Double
    )

    companion object {
        const val KEY_DAYS_WEEK = "cost_days_week"
        const val KEY_HOURS_DAY = "cost_hours_day"
        const val KEY_KM_DAY = "cost_km_day"
        const val KEY_WEEKLY_GOAL = "cost_weekly_goal"
        const val KEY_RENT_MONTH = "cost_rent_month"
        const val KEY_MAINTENANCE_MONTH = "cost_maintenance_month"
        const val KEY_OTHER_MONTH = "cost_other_month"
        const val KEY_FUEL_TYPE = "cost_fuel_type"
        const val KEY_EFFICIENCY = "cost_efficiency"
        const val KEY_ENERGY_PRICE = "cost_energy_price"
        const val KEY_COST_PER_HOUR = "cost_per_hour"
        const val KEY_FIXED_COST_PER_KM = "fixed_cost_per_km"
        const val KEY_ENERGY_COST_PER_KM = "energy_cost_per_km"
    }
}
