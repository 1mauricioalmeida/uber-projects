# Pesquisa de necessidades do passageiro e acessibilidade — 2026-08

## Objetivo

Transformar o Hospitalidade a Bordo em uma camada de comunicação discreta e inclusiva durante a viagem, sem exigir que o passageiro fale, se exponha ou declare um diagnóstico, e sem aumentar a distração do motorista.

## Evidências e referências pesquisadas

### Uber e preferências de viagem

A própria Uber Comfort oferece preferências de conversa e temperatura e informa essas preferências ao motorista. Isso valida o valor de permitir que o passageiro comunique necessidades de conforto antes ou durante a viagem sem precisar iniciar uma conversa verbal.

Referências:
- https://www.uber.com/br/pt-br/ride/uber-comfort/
- https://www.uber.com/br/pt-br/newsroom/uber-lanca-no-brasil-categoria-que-permite-escolher-temperatura-do-carro-e-nivel-de-conversa-antes-da-viagem/

### Acessibilidade e comunicação

A Uber mantém recursos de acessibilidade para usuários e motoristas, incluindo preferências de comunicação, suporte a pessoas surdas ou com deficiência auditiva, leitores de tela e autoidentificação opcional em alguns contextos. A orientação da Uber para atender pessoas com deficiência enfatiza perguntar como ajudar, ouvir a solicitação e respeitar a autonomia da pessoa.

Referências:
- https://www.uber.com/br/pt-br/newsroom/acessibilidade-aplicativo-pessoas-deficiencia/
- https://www.uber.com/us/en/drive/accessibility/
- https://help.uber.com/en/driving-and-delivering/article/communication-preferences-for-drivers-and-couriers-

### Acessibilidade cognitiva

A W3C recomenda formulários curtos, linguagem clara, controles previsíveis, opções válidas pré-definidas, grandes áreas clicáveis, instruções simples e personalização. Também observa que muitas deficiências cognitivas são invisíveis e que usuários podem não ter diagnóstico ou não querer revelá-lo.

Referências:
- https://www.w3.org/WAI/tutorials/forms/
- https://www.w3.org/WAI/people-use-web/abilities-barriers/cognitive/
- https://www.w3.org/WAI/WCAG2/supplemental/objectives/o8-personalization/

### Relatos públicos de passageiros e motoristas

Discussões públicas de usuários mostram conflitos recorrentes em torno de conversa, volume da música, temperatura e expectativas de cordialidade. Esses relatos são anedóticos e não representam toda a população, mas reforçam que preferências silenciosas e explícitas podem evitar interpretações erradas entre passageiro e motorista.

Exemplo pesquisado:
- https://www.reddit.com/r/conversas/comments/1jwq7fp/

### Qualidade percebida

Estudos recentes de ridesourcing associam satisfação a segurança da plataforma, competência do motorista e conforto do veículo. Trabalho sobre qualidade na Uber também encontra evidências de que feedback e preferências dos usuários podem influenciar o comportamento dos motoristas.

Referências:
- https://discovery.ucl.ac.uk/id/eprint/10189525/
- https://www.nber.org/papers/w33087

## Princípio de produto

O app deve perguntar **o que a pessoa precisa**, e não **qual condição ela tem**.

Evitar perguntas como:
- “Você tem alguma deficiência?”
- “Você é autista?”
- “Você tem ansiedade?”

Preferir necessidades acionáveis como:
- “Prefiro não conversar.”
- “Fale comigo apenas se for necessário.”
- “Tenho dificuldade para falar; prefiro usar esta tela.”
- “Posso precisar de alguns segundos para responder.”
- “Prefiro pouco som / silêncio.”
- “Preciso de ajuda.”

Isso atende pessoas com deficiência, neurodivergência, barreiras de idioma, timidez, ansiedade social, cansaço ou simplesmente uma preferência pessoal sem criar um cadastro sensível.

## Requisitos derivados

1. **Comunicação sem fala como caminho principal, não exceção**
   - todas as preferências essenciais devem ser selecionáveis por toque;
   - nenhuma escolha deve exigir justificativa;
   - observação livre permanece opcional.

2. **Perfil sensorial simples**
   - temperatura;
   - música/volume;
   - silêncio;
   - conversa;
   - evitar alertas visuais piscantes no app do passageiro.

3. **Preferências de comunicação**
   - pode conversar;
   - prefiro silêncio;
   - fale somente se necessário;
   - prefiro me comunicar por esta tela;
   - preciso de mais tempo para responder.

4. **Ajuda com baixa carga cognitiva**
   - botão “Preciso de ajuda” sempre visível e inequívoco;
   - depois do toque, oferecer poucas categorias grandes em vez de exigir texto;
   - permitir cancelar ou corrigir uma seleção facilmente.

5. **Não exigir identificação de deficiência**
   - não armazenar diagnóstico;
   - não inferir deficiência a partir das escolhas;
   - registrar somente preferências operacionais da viagem.

6. **Motorista sem distração**
   - música automatizável não gera tarefa manual;
   - solicitações manuais aparecem em notificação persistente;
   - voz, heads-up e vibração são configuráveis pelo motorista;
   - urgências têm tratamento separado;
   - nenhuma tela complexa deve exigir uso com o veículo em movimento.

7. **Métricas de qualidade sem perfilamento sensível**
   - pedido recebido em;
   - pedido atendido em;
   - tempo de resposta;
   - preferência atendida ou não;
   - avaliação da viagem;
   - correlação agregada com ganhos, categoria, horário e duração;
   - não criar métricas do tipo “passageiro com deficiência”.

## Próximos incrementos recomendados

### Passageiro

Criar uma seção curta “Como você prefere esta viagem?” com linguagem inclusiva e sem mencionar deficiência. Permitir múltiplas necessidades compatíveis, em vez de forçar uma única escolha de interação.

### Motorista

Transformar pedidos em ações operacionais: automáticos quando possível e persistentes quando manuais. O motorista escolhe como quer receber cada tipo de evento.

### Pesquisa futura

Validar o fluxo com pessoas com diferentes necessidades de comunicação e sensoriais antes de considerar o recurso concluído. A pesquisa deve medir facilidade, constrangimento percebido, clareza e autonomia, não pedir diagnóstico quando ele não for necessário.
