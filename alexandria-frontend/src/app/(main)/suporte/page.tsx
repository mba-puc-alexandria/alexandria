"use client";

import { HelpCircle, Mail, BookOpen } from "lucide-react";

const faqs = [
  {
    question: "Como adiciono um livro à minha biblioteca?",
    answer: 'Acesse "Explorar", encontre o livro desejado e clique em "Adicionar à Biblioteca".',
  },
  {
    question: "Como acompanho meu progresso de leitura?",
    answer: 'Abra o livro pelo leitor — o progresso é salvo automaticamente conforme você avança.',
  },
  {
    question: "Os livros estão disponíveis em português?",
    answer: "O acervo inclui obras em vários idiomas. Use os filtros na página Explorar para buscar por idioma.",
  },
];

export default function SuportePage() {
  return (
    <div className="px-6 md:px-8 pt-8 md:pt-12 pb-8 flex flex-col gap-8 max-w-2xl">
      <div>
        <h1 className="font-serif font-bold text-brown text-2xl">Suporte</h1>
        <p className="text-slate text-sm mt-1">Dúvidas e ajuda para usar o Alexandria</p>
      </div>

      {/* FAQ */}
      <section className="flex flex-col gap-3">
        <div className="flex items-center gap-2 mb-1">
          <HelpCircle size={14} className="text-terra" />
          <span className="text-brown text-xs font-bold uppercase tracking-widest">Perguntas Frequentes</span>
        </div>

        <div className="flex flex-col gap-3">
          {faqs.map((faq, i) => (
            <div key={i} className="bg-cream-dark rounded-xl p-5 flex flex-col gap-2">
              <p className="text-brown font-bold text-sm">{faq.question}</p>
              <p className="text-slate text-sm leading-relaxed">{faq.answer}</p>
            </div>
          ))}
        </div>
      </section>

      {/* Contato */}
      <section className="flex flex-col gap-3">
        <div className="flex items-center gap-2 mb-1">
          <Mail size={14} className="text-terra" />
          <span className="text-brown text-xs font-bold uppercase tracking-widest">Contato</span>
        </div>

        <div className="bg-cream-dark rounded-xl p-5 flex flex-col gap-2">
          <p className="text-slate text-sm leading-relaxed">
            Encontrou um problema ou tem uma sugestão? Entre em contato pelo e-mail:
          </p>
          <a
            href="mailto:suporte@alexandria.pucsp.br"
            className="text-terra font-bold text-sm hover:underline w-fit"
          >
            suporte@alexandria.pucsp.br
          </a>
        </div>
      </section>

      {/* Sobre */}
      <section className="flex flex-col gap-3">
        <div className="flex items-center gap-2 mb-1">
          <BookOpen size={14} className="text-terra" />
          <span className="text-brown text-xs font-bold uppercase tracking-widest">Sobre</span>
        </div>

        <div className="bg-cream-dark rounded-xl p-5">
          <p className="text-slate text-sm leading-relaxed">
            Alexandria é uma biblioteca digital desenvolvida como projeto integrador na PUC-SP.
            O acervo é alimentado pelo projeto Gutenberg e por obras enviadas pelos usuários.
          </p>
        </div>
      </section>
    </div>
  );
}
